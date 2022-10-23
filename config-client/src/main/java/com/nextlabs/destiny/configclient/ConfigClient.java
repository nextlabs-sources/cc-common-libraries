package com.nextlabs.destiny.configclient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bluejungle.framework.crypt.ReversibleEncryptor;
import com.nextlabs.destiny.logmanager.LogManagerClient;

/**
 * ConfigClient maintains the configuration list for application. The init method should be called as soon as the
 * Application is initialized and before using the configurations. The configurations can be obtained using get()
 * methods which returns a Config supplier.
 *
 * @author Sachindra Dasun
 */
public class ConfigClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigClient.class);
    private static final Map<String, Config> configMap = new ConcurrentHashMap<>();
    static final ReversibleEncryptor REVERSIBLE_ENCRYPTOR = new ReversibleEncryptor();
    private static final String ENCRYPTED_VALUE_PREFIX = "{cipher}";
    static final String CONFIG_KEY_CC_HOME = "cc.home";
    private static final String CONFIG_KEY_SERVER_HOSTNAME = "server.hostname";
    private static final String AUTHORIZATION_PROPERTY = "Basic %s";

    private static String applicationName = "application";
    private static String configServiceUrl = "";
    private static String configServiceUsername = "";
    private static String configServicePassword = "";
    private static ConfigRefresher configRefresher;

    private ConfigClient() {
    }

    /**
     * Initialize the configuration client. If application name is not provided, only the default configurations will be
     * available.
     *
     * @param applicationName application name used to load configurations
     * @throws Exception if an error occurred
     */
    public static void init(String applicationName) throws IOException {
        if (applicationName != null && !applicationName.isEmpty()) {
            ConfigClient.applicationName = applicationName;
        }
        
        Properties bootstrapProperties = new Properties();
        bootstrapProperties.load(new FileInputStream(System.getProperty("spring.cloud.bootstrap.location")));
        configServiceUrl = bootstrapProperties.getProperty("spring.cloud.config.uri");
        configServiceUsername = bootstrapProperties.getProperty("spring.cloud.config.username");
        configServicePassword = bootstrapProperties.getProperty("spring.cloud.config.password");

        // Enhance security, make sure communication happens in secured channel
        if(configServiceUrl == null) {
            throw new IllegalArgumentException("Invalid URL. URL cannot be null.");
        }

        refresh();

        boolean enableLogManager = get("logger.manager.enabled", false).toBoolean();
        boolean enableConfigRefresher = get("config.update.refresher.enabled", false).toBoolean();
        boolean enableLoggerRefresher = get("logger.update.refresher.enabled", false).toBoolean();
        boolean enableSecureStoreRefresher = get("secureStore.update.refresher.enabled", false).toBoolean();

        // Logger refresher will be enabled only if log manager is enabled.
        enableLoggerRefresher = enableLogManager && enableLoggerRefresher;

        if (enableLogManager) {
            LogManagerClient.refresh(configServiceUrl, configServiceUsername, configServicePassword);
        }

        if (enableConfigRefresher || enableLoggerRefresher) {
            configRefresher = new ConfigRefresher(enableConfigRefresher, enableLoggerRefresher, enableSecureStoreRefresher);
            new Thread(configRefresher).start();
        }
    }

    /**
     * Return the application name.
     *
     * @return the application name
     */
    public static String getApplicationName() {
        return applicationName;
    }

    /**
     * Access configuration service and return the configuration file content as an InputStream.
     *
     * @param uri URI which is relative to the configuration service uri defined in bootstrap.properties file
     * @return an InputStream for configuration content
     */
    public static InputStream getContent(String uri) {
        byte[] authorization = String.format("%s:%s", configServiceUsername, decryptIfEncrypted(configServicePassword))
                .getBytes(StandardCharsets.UTF_8);
        String configurationPropertiesURL = String.format("%s/%s", configServiceUrl, uri);
        while (true) {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(configurationPropertiesURL).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization",
                        String.format(AUTHORIZATION_PROPERTY, Base64.getEncoder().encodeToString(authorization)));
                try (InputStream inputStream = connection.getInputStream()) {
                    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = inputStream.read(buffer)) > -1) {
                            byteArrayOutputStream.write(buffer, 0, length);
                        }
                        byteArrayOutputStream.flush();
                        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                    }
                }
            } catch (IOException e) {
                LOGGER.warn("Connection failed. Retrying.", e);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    LOGGER.error("Error occurred", e);
                    return null;
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }

    /**
     * Perform configuration refresh. This method should not be called outside the config-client.
     *
     * @throws Exception if an error occurred
     */
    public static void refresh() throws IOException {
        Properties properties = new Properties();
        try (InputStream inputStream = getContent(String.format("%s-default.properties", applicationName))) {
            properties.load(inputStream);
        }
        overrideProperties(properties);
        update(properties);
        LOGGER.info("Configurations refreshed");
    }

    public static void downloadSecureStore() throws IOException {
        InputStream inputStream = getContent("secure-store/download");
        if (inputStream == null) {
            return;
        }
        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry fileEntry = zipInputStream.getNextEntry();

            while(fileEntry != null) {
                byte[] buffer = new byte[1024];
                int length;
                File unzipFile = new File(String.join(File.separator, System.getProperty(CONFIG_KEY_CC_HOME, "")
                                , "server", "certificates", fileEntry.getName()));
                try(FileOutputStream fileOutputStream = new FileOutputStream(unzipFile)) {
                    while((length = zipInputStream.read(buffer)) > 0) {
                        fileOutputStream.write(buffer, 0, length);
                    }
                    fileOutputStream.flush();
                }
                fileEntry = zipInputStream.getNextEntry();
            }
            zipInputStream.closeEntry();
        }

        LOGGER.info("Secure stores downloaded");

    }

    /**
     * Override properties from configurations found in local configuration file.
     *
     * @param properties the properties to override
     */
    private static void overrideProperties(Properties properties) throws IOException {
        String configPath = System.getProperty("server.config.path");
        if (configPath != null) {
            File configFile = Paths.get(configPath, String.format("%s-local.properties", applicationName)).toFile();
            if (configFile.exists()) {
                Properties localProperties = new Properties();
                try (FileInputStream fileInputStream = new FileInputStream(configFile)) {
                    localProperties.load(fileInputStream);
                }
                LOGGER.info("Configurations overridden from local file: {}", localProperties.entrySet());
                properties.putAll(localProperties);
            }
        }
    }

    /**
     * Update the configuration map. This method should not be called outside the config-client.
     *
     * @param properties the configuration value source
     */
    public static synchronized void update(Properties properties) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            Config config = configMap.computeIfAbsent(entry.getKey().toString(), Config::new);
            if (entry.getValue() != null) {
                String value = entry.getValue().toString();
                if (value.startsWith(ENCRYPTED_VALUE_PREFIX)) {
                    value = REVERSIBLE_ENCRYPTOR.decrypt(value.substring(value.indexOf(ENCRYPTED_VALUE_PREFIX)
                            + ENCRYPTED_VALUE_PREFIX.length()));
                }
                config.setValue(getInterpolatedValue(value));
            }
        }
        addDefaultConfigurations();
    }

    private static String getInterpolatedValue(String value) {
        if (value.contains("${")) {
            for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
                value = value.replace(String.format("${%s}", entry.getKey().toString()), entry.getValue().toString());
            }
        }
        return value;
    }

    private static void addDefaultConfigurations() {
        if (!configMap.containsKey(CONFIG_KEY_CC_HOME)) {
            Config ccHomeConfig = new Config(CONFIG_KEY_CC_HOME, System.getProperty(CONFIG_KEY_CC_HOME, ""));
            configMap.put(ccHomeConfig.getKey(), ccHomeConfig);
        }

        if (!configMap.containsKey(CONFIG_KEY_SERVER_HOSTNAME)) {
            Config serverHostnameConfig = new Config(CONFIG_KEY_SERVER_HOSTNAME, System.getProperty(CONFIG_KEY_SERVER_HOSTNAME, ""));
            configMap.put(serverHostnameConfig.getKey(), serverHostnameConfig);
        }
    }

    /**
     * Returns the configuration object for the given key. If the configuration object not exists, a new
     * configuration will be added with null value.
     *
     * @param key the configuration key
     * @return the configuration object
     */
    public static Config get(String key) {
        return get(key, null);
    }

    /**
     * Returns the configuration object for the given key. If the configuration object does not exists, a new
     * configuration will be created with the value as the given String value.
     *
     * @param key          the configuration key
     * @param defaultValue default value of the configuration
     * @return the configuration object
     */
    public static Config get(String key, String defaultValue) {
        return configMap.computeIfAbsent(key, configKey -> new Config(configKey, defaultValue));
    }

    /**
     * If configuration refresher or logger refresher is enabled, it is important to call stop at the end of the
     * application to properly close the ActiveMQ connection.
     */
    public static void close() {
        if (configRefresher != null) {
            configRefresher.close();
        }
    }

    /**
     * Returns the configuration object for the given key. If the configuration object does not exists, a new
     * configuration will be created with the value as the given long value.
     *
     * @param key          the configuration key
     * @param defaultValue default value of the configuration
     * @return the configuration object
     */
    public static Config get(String key, long defaultValue) {
        return get(key, String.valueOf(defaultValue));
    }

    /**
     * Returns the configuration object for the given key. If the configuration object does not exists, a new
     * configuration will be created with the value as the given int value.
     *
     * @param key          the configuration key
     * @param defaultValue default value of the configuration
     * @return the configuration object
     */
    public static Config get(String key, int defaultValue) {
        return get(key, String.valueOf(defaultValue));
    }

    /**
     * Returns the configuration object for the given key. If the configuration object does not exists, a new
     * configuration will be created with the value as the given double value.
     *
     * @param key          the configuration key
     * @param defaultValue default value of the configuration
     * @return the configuration object
     */
    public static Config get(String key, double defaultValue) {
        return get(key, String.valueOf(defaultValue));
    }

    /**
     * Returns the configuration object for the given key. If the configuration object does not exists, a new
     * configuration will be created with the value as the given boolean value.
     *
     * @param key          the configuration key
     * @param defaultValue default value of the configuration
     * @return the configuration object
     */
    public static Config get(String key, boolean defaultValue) {
        return get(key, String.valueOf(defaultValue));
    }

    /**
     * Returns all configurations matching with the given prefix.
     *
     * @param prefix prefix to filter the configurations
     * @return the matching configurations as a List
     */
    public static List<Config> getAll(String prefix) {
        return configMap.values().stream()
                .filter(config -> prefix == null || prefix.isEmpty() || config.getKey().startsWith(prefix))
                .collect(Collectors.toList());
    }

    /**
     * Returns all configurations.
     *
     * @return all configurations as a List
     */
    public static List<Config> getAll() {
        return getAll(null);
    }

    /**
     * Clear ThreadLocal values.
     */
    public static void clear() {
        for (Map.Entry<String, Config> entry : configMap.entrySet()) {
            Config config = entry.getValue();
            if (config != null) {
                entry.getValue().clear();
            }
        }
    }

    public static String decryptIfEncrypted(String text) {
        if (text != null && text.startsWith(ENCRYPTED_VALUE_PREFIX)) {
            return REVERSIBLE_ENCRYPTOR.decrypt(text);
        }
        return text;
    }

}

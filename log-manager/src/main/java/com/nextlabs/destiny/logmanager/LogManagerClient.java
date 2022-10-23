package com.nextlabs.destiny.logmanager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.composite.CompositeConfiguration;

import com.bluejungle.framework.crypt.ReversibleEncryptor;

/**
 * Log manager client obtain logger configurations from configuration service and re-configure the LogManager.
 *
 * @author Sachindra Dasun
 */
public class LogManagerClient {

    private static final String AUTHORIZATION_PROPERTY = "Basic %s";
    private static final String ENCRYPTED_VALUE_PREFIX = "{cipher}";
    private static final Logger LOGGER = LogManager.getLogger(LogManagerClient.class);
    private static final String LOGGING_CONFIG_ENDPOINT = "%s/logger-config/get";
    private static final ReversibleEncryptor REVERSIBLE_ENCRYPTOR = new ReversibleEncryptor();
    private static String configServicePassword;
    private static String configServiceUrl;
    private static String configServiceUsername;
    private static boolean initialized;

    private LogManagerClient() {
    }

    /**
     * Init and setup the loggers.
     *
     * @param configServiceUrl      configuration service URL
     * @param configServiceUsername configuration service username
     * @param configServicePassword configuration service password
     */
    public static void refresh(String configServiceUrl, String configServiceUsername,
                               String configServicePassword) {
        init(configServiceUrl, configServiceUsername, configServicePassword);
        try {
            refresh();
        } catch (Exception e) {
            LOGGER.error("Error in refreshing loggers", e);
        }
    }

    public static void init(String configServiceUrl, String configServiceUsername,
                            String configServicePassword) {
        LogManagerClient.configServiceUrl = configServiceUrl;
        LogManagerClient.configServiceUsername = configServiceUsername;
        LogManagerClient.configServicePassword = configServicePassword;

        // Enhance security, make sure communication happens in secured channel
        if (configServiceUrl == null) {
            throw new IllegalArgumentException("Invalid URL. URL cannot be null.");
        }
    }

    public static void refresh() {
        String loggingConfigUrl = String.format(LOGGING_CONFIG_ENDPOINT, configServiceUrl);
        LOGGER.log(initialized ? Level.INFO : Level.DEBUG,
                "Refreshing logger configurations from URL: {}", loggingConfigUrl);
        byte[] authorization = String.format("%s:%s", configServiceUsername,
                decryptIfEncrypted(configServicePassword)).getBytes(StandardCharsets.UTF_8);
        while (true) {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(loggingConfigUrl).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization",
                        String.format(AUTHORIZATION_PROPERTY, Base64.getEncoder().encodeToString(authorization)));
                try (InputStream inputStream = connection.getInputStream()) {
                    LOGGER.log(initialized ? Level.INFO : Level.DEBUG,
                            "Logger configurations received from URL: {}", loggingConfigUrl);
                    ObjectMapper objectMapper = new ObjectMapper();
                    List<String> loggerConfigs = objectMapper.readValue(inputStream,
                            objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                    configure(loggerConfigs);
                    return;
                }
            } catch (IOException e) {
                LOGGER.warn("Connection failed. Retrying.", e);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    LOGGER.error("Error occurred", e);
                    return;
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }

    public static void configure(List<String> loggerConfigs) {
        String localLoggingConfig = getLocalLoggingConfig();
        if (localLoggingConfig != null) {
            loggerConfigs.add(localLoggingConfig);
        }
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        CompositeConfiguration compositeConfiguration = new CompositeConfiguration(
                loggerConfigs.stream()
                        .map(loggerConfig -> {
                            try {
                                Configuration configuration = ConfigurationFactory.getInstance()
                                        .getConfiguration(context, new ConfigurationSource(
                                                new ByteArrayInputStream(loggerConfig.getBytes())));
                                if (configuration instanceof AbstractConfiguration) {
                                    return (AbstractConfiguration) configuration;
                                }
                            } catch (IOException e) {
                                LOGGER.error("Error occurred when creating the logger config", e);
                            }
                            return null;
                        }).filter(Objects::nonNull)
                        .collect(Collectors.toList())
        );
        context.start(compositeConfiguration);
        initialized = true;
    }

    private static String getLocalLoggingConfig() {
        String configPath = System.getProperty("server.config.path");
        try {
            if (configPath != null) {
                Path configFilePath = Paths.get(configPath, "log4j2-local.xml");
                if (configFilePath.toFile().exists()) {
                    String loggingConfig = new String(Files.readAllBytes(configFilePath), Charset.defaultCharset());
                    LOGGER.info("Local logging configuration found in {}", configFilePath);
                    return loggingConfig;
                }
            } else {
                LOGGER.debug("server.config.path system property not found");
            }
        } catch (IOException e) {
            LOGGER.error("Error in reading local logger config from " + configPath, e);
        }
        LOGGER.debug("No local logging configurations found");
        return null;
    }

    public static String decryptIfEncrypted(String text) {
        if (text != null && text.startsWith(ENCRYPTED_VALUE_PREFIX)) {
            return REVERSIBLE_ENCRYPTOR.decrypt(text);
        }
        return text;
    }

}

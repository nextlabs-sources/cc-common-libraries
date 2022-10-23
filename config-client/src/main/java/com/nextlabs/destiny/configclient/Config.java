package com.nextlabs.destiny.configclient;

/**
 * Config object wrap the value of a configuration. A ThreadLocal value of the configuration will
 * be provided when toTYPE methods are used without arguments. If it is required to access the non thread local value
 * of the configuration, the toTYPE(false) methods should be used.
 *
 * @author Sachindra Dasun
 */
public class Config {

    private String key;
    private String value;

    private ThreadLocal<String> threadLocalValue = ThreadLocal.withInitial(() -> value);

    /**
     * Creates a new configuration with the given key and null value.
     *
     * @param key the configuration key
     */
    public Config(String key) {
        this.key = key;
    }

    /**
     * Creates a new configuration with the given key and value.
     *
     * @param key   the configuration key
     * @param value the configuration value
     */
    public Config(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Sets the configuration value.
     *
     * @param value the new value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns the ThreadLocal value of the configuration as a String after interpolating with given
     * configurations.
     *
     * @param configs the configurations to interpolate
     * @return the configuration value as a String value
     */
    public String toString(Config... configs) {
        return toString(true, configs);
    }

    /**
     * Returns the configuration value as a String after interpolating with given configurations.
     *
     * @param threadLocal if true, the ThreadLocal value is returned
     * @param configs     the configurations to interpolate
     * @return the configuration value as a String value
     */
    public String toString(boolean threadLocal, Config... configs) {
        String configValue = toString(threadLocal);
        if (configs != null) {
            for (Config config : configs) {
                if (!config.isEmpty(threadLocal)) {
                    configValue = configValue.replace(String.format("${%s}", config.getKey()), config.toString(threadLocal));
                }
            }
        }
        return configValue;
    }

    /**
     * Returns the configuration value as a String value.
     *
     * @param threadLocal if true, the ThreadLocal value is returned
     * @return the configuration value as a String value
     */
    public String toString(boolean threadLocal) {
        return threadLocal ? threadLocalValue.get() : value;
    }

    /**
     * Gets the configuration key.
     *
     * @return the configuration key
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the ThreadLocal value of the configuration as a String value.
     *
     * @return the configuration value as a String value
     */
    public String toString() {
        return toString(true);
    }

    /**
     * Returns the configuration value as a double value.
     *
     * @param threadLocal if true, the ThreadLocal value is returned
     * @return the configuration value as a double value
     */
    public double toDouble(boolean threadLocal) {
        return Double.parseDouble(threadLocal ? threadLocalValue.get() : value);
    }

    /**
     * Returns the ThreadLocal value of the configuration as a double value.
     *
     * @return the configuration value as a double value
     */
    public double toDouble() {
        return toInt(true);
    }

    /**
     * Returns the configuration value as an int value.
     *
     * @param threadLocal if true, the ThreadLocal value is returned
     * @return the configuration value as an int value
     */
    public int toInt(boolean threadLocal) {
        return Integer.parseInt(threadLocal ? threadLocalValue.get() : value);
    }

    /**
     * Returns the ThreadLocal value of the configuration as an int value.
     *
     * @return the configuration value as an int value
     */
    public int toInt() {
        return toInt(true);
    }

    /**
     * Returns the ThreadLocal value of the configuration as a long value.
     *
     * @return the configuration value as a long value
     */
    public long toLong() {
        return toLong(true);
    }

    /**
     * Returns the configuration value as a long value.
     *
     * @param threadLocal if true, the ThreadLocal value is returned
     * @return the configuration value as a long value
     */
    public long toLong(boolean threadLocal) {
        return Long.parseLong(threadLocal ? threadLocalValue.get() : value);
    }

    /**
     * Returns the ThreadLocal value of the configuration as a boolean value.
     *
     * @return the configuration value as a boolean value
     */
    public boolean toBoolean() {
        return toBoolean(true);
    }

    /**
     * Returns the configuration value as a boolean value.
     *
     * @param threadLocal if true, the ThreadLocal value is returned
     * @return the configuration value as a boolean value
     */
    public boolean toBoolean(boolean threadLocal) {
        return Boolean.parseBoolean(threadLocal ? threadLocalValue.get() : value);
    }

    /**
     * Returns true if ThreadLocal value of the configuration is empty.
     *
     * @return true if ThreadLocal value of the configuration is empty
     */
    public boolean isEmpty() {
        return isEmpty(true);
    }

    /**
     * Returns true if configuration  value is empty.
     *
     * @param threadLocal if true, the ThreadLocal value is checked.
     * @return true if the configuration value is empty
     */
    public boolean isEmpty(boolean threadLocal) {
        String configValue = threadLocal ? threadLocalValue.get() : value;
        return configValue == null || configValue.isEmpty();
    }

    /**
     * Clear ThreadLocal value.
     */
    public void clear() {
        threadLocalValue.remove();
    }

}

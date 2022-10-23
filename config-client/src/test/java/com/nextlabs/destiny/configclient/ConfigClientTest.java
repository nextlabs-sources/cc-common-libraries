package com.nextlabs.destiny.configclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Scanner;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for configuration client.
 *
 * @author Sachindra Dasun
 */
public class ConfigClientTest {

    private static final String CC_HOME = "D:\\installations\\cc\\PolicyServer";
    private static final String CONFIG_LOCATION = "./src/test/resources";
    private static final String BOOTSTRAP_CONFIG_LOCATION = "./src/test/resources/bootstrap.properties";
    private static final String CC_HOSTNAME = "alaid.nextlabs.com";

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.setProperty("cc.home", CC_HOME);
        System.setProperty("spring.cloud.bootstrap.location", BOOTSTRAP_CONFIG_LOCATION);
        System.setProperty("server.hostname", CC_HOSTNAME);
        System.setProperty("server.config.path", CONFIG_LOCATION);
        ConfigClient.init("console");
    }

    @AfterClass
    public static void afterClass() {
        ConfigClient.close();
    }

    @Test
    public void testLoadConfigurationFromUrl() {
        assertFalse(ConfigClient.get("db.comboPooledDataSource.maxPoolSize").isEmpty());
    }

    @Test
    public void testConfigOverride() {
        Config config = ConfigClient.get("application.version");
        assertNotNull(config.toString());
        assertEquals("10.0.0", config.toString());
    }

    @Test
    public void testPropertyInterpolation() {
        assertTrue(ConfigClient.get("server.license.dir").toString().contains(CC_HOME));
    }

    @Test
    public void testDefaultValue() {
        Config defaultValueConfig = ConfigClient.get("property.default.value", 10);
        assertNotNull(defaultValueConfig);
        assertFalse(defaultValueConfig.isEmpty());
        assertEquals(10, defaultValueConfig.toInt());
    }

    @Test
    public void testGetConfiguration() {
        Config config = ConfigClient.get("db.comboPooledDataSource.maxPoolSize");
        assertNotNull(config);
        assertNotNull(config.toString());
        assertTrue(config.toLong() > 0);
        assertTrue(config.toInt() > 0);
        assertTrue(config.toDouble() > 0);
    }

    @Test
    public void testGetConfigurationContent() throws Exception {
        InputStream inputStream = ConfigClient.getContent("dms/default/master/configuration.xml");
        assertNotNull(inputStream);
        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
        String content = scanner.hasNext() ? scanner.next() : "";
        System.out.println(content);
        assertTrue(!content.isEmpty());
    }

}

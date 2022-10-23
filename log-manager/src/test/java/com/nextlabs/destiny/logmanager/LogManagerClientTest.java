package com.nextlabs.destiny.logmanager;

import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for log manager client.
 *
 * @author Sachindra Dasun
 */
public class LogManagerClientTest {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String CC_HOME = "D:\\installations\\cc\\PolicyServer";
    private static final String CONFIG_PATH = "./src/test/resources/";
    private static final String BOOTSTRAP_CONFIG_LOCATION = "./src/test/resources/bootstrap.properties";

    @BeforeClass
    public static void beforeClass() throws IOException {
        System.setProperty("cc.home", CC_HOME);
        System.setProperty("server.config.path", CONFIG_PATH);
        System.setProperty("spring.cloud.bootstrap.location", BOOTSTRAP_CONFIG_LOCATION);
        Properties bootstrapProperties = new Properties();
        bootstrapProperties.load(new FileInputStream(System.getProperty("spring.cloud.bootstrap.location")));
        String configServiceUrl = bootstrapProperties.getProperty("spring.cloud.config.uri");
        String configServiceUsername = bootstrapProperties.getProperty("spring.cloud.config.username");
        String configServicePassword = bootstrapProperties.getProperty("spring.cloud.config.password");
        LogManagerClient.init(configServiceUrl, configServiceUsername, configServicePassword);
    }

    @Test
    public void testLogRefresh() throws Exception {
        LOGGER.trace("Before Trace log test");
        LOGGER.debug("Before Debug log test");
        LOGGER.info("Before Info log test");
        LOGGER.warn("Before Warn log test");
        LOGGER.error("Before Error log test");
        LogManagerClient.refresh();
        LOGGER.trace("After Trace log test");
        LOGGER.debug("After Debug log test");
        LOGGER.info("After Info log test");
        LOGGER.warn("After Warn log test");
        LOGGER.error("After  Error log test");
        assertTrue(true);
    }

}

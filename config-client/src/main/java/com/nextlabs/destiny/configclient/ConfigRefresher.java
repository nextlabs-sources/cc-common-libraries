package com.nextlabs.destiny.configclient;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import com.nextlabs.destiny.configclient.listeners.SecureStoreUpdateListener;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nextlabs.destiny.configclient.listeners.ConfigUpdateListener;
import com.nextlabs.destiny.configclient.listeners.LoggerUpdateListener;

/**
 * Create ActiveMQ connection and subscribe to messages received to a application specific configuration update topic.
 * When a configuration update message is received, the configurations will be refreshed using the configuration
 * service. The additional parameters of the configuration factory can be customized by creating properties with the
 * prefix "config.activeMQConnectionFactory".
 *
 * @author Sachindra Dasun
 */
class ConfigRefresher implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigRefresher.class);
    private static final String CONNECTION_FACTORY_CONFIG_PREFIX = "config.activeMQConnectionFactory";
    private static final String DEFAULT_CONFIG_UPDATE_TOPIC = "jms/cc.%application.name%.config.update";
    private static final String DEFAULT_LOGGER_UPDATE_TOPIC = "jms/cc.%application.name%.logger.update";
    private static final String DEFAULT_SECURE_STORE_UPDATE_TOPIC = "jms/cc.%application.name%.securestore.update";
    private static final Config brokerUrl = ConfigClient.get("config.activeMQConnectionFactory.brokerURL");
    private static final boolean brokerSslEnabled = ConfigClient.get("activemq.broker.ssl.enabled", true).toBoolean();
    private static final Config configUpdateTopic = ConfigClient.get("jms.config.update.topic",
            DEFAULT_CONFIG_UPDATE_TOPIC);
    private static final Config loggerUpdateTopic = ConfigClient.get("jms.logger.update.topic",
            DEFAULT_LOGGER_UPDATE_TOPIC);
    private static final Config secureStoreUpdateTopic = ConfigClient.get("jms.securestore.update.topic",
            DEFAULT_SECURE_STORE_UPDATE_TOPIC);

    private boolean enableConfigRefresher;
    private boolean enableLoggerRefresher;
    private boolean enableSecureStoreRefresher;
    private Connection connection;
    private Session session;
    private MessageConsumer configUpdateConsumer;
    private MessageConsumer loggerUpdateConsumer;
    private MessageConsumer secureStoreUpdateConsumer;

    ConfigRefresher(boolean enableConfigRefresher, boolean enableLoggerRefresher, boolean enableSecureStoreRefresher) {
        this.enableConfigRefresher = enableConfigRefresher;
        this.enableLoggerRefresher = enableLoggerRefresher;
        this.enableSecureStoreRefresher = enableSecureStoreRefresher;
    }

    public void run() {
        LOGGER.info("Starting refresher: Configurations={}, Loggers={}, SecureStores={}",
                enableConfigRefresher, enableLoggerRefresher, enableSecureStoreRefresher);
        if (brokerUrl.isEmpty()) {
            LOGGER.info("ActiveMQ broker URL is empty and refresher will not be started.");
            return;
        }
        try {
            ActiveMQConnectionFactory connectionFactory = brokerSslEnabled ?
                    new ActiveMQSslConnectionFactory() : new ActiveMQConnectionFactory();
            customizeConnectionFactory(connectionFactory);
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            if (enableConfigRefresher) {
                createConfigUpdateConsumer(session, new ConfigUpdateListener());
            }
            if (enableLoggerRefresher) {
                createLoggerUpdateConsumer(session, new LoggerUpdateListener());
            }
            if (enableSecureStoreRefresher) {
                createSecureStoreUpdateConsumer(session, new SecureStoreUpdateListener());
            }
        } catch (JMSException e) {
            LOGGER.error(String.format("Error in initializing configuration refresher. Configuration changes will not" +
                    " be refreshed. Please check if application can connect to the ActiveMQ broker at %s and restart " +
                    "the application %s to retry.", brokerUrl, ConfigClient.getApplicationName()), e);
        }
        LOGGER.info("Configuration refresher started and listening for configuration updates.");
    }

    private void customizeConnectionFactory(ActiveMQConnectionFactory connectionFactory) {
        for (Config config : ConfigClient.getAll(CONNECTION_FACTORY_CONFIG_PREFIX)) {
            try {
                BeanUtils.setProperty(connectionFactory,
                        config.getKey().replace(CONNECTION_FACTORY_CONFIG_PREFIX, "")
                                .replace(".", ""),
                        config.toString());
            } catch (Exception e) {
                LOGGER.error(String.format("Error in setting ComboPooledDataSource property %s to %s", config.getKey(),
                        config), e);
            }
        }
    }

    private void createConfigUpdateConsumer(Session session, MessageListener configUpdateListener) throws JMSException {
        String topicName = configUpdateTopic.toString().replace("%application.name%",
                ConfigClient.getApplicationName());
        this.configUpdateConsumer = session.createConsumer(session.createTopic(topicName));
        this.configUpdateConsumer.setMessageListener(configUpdateListener);
    }

    private void createLoggerUpdateConsumer(Session session, MessageListener loggerUpdateListener) throws JMSException {
        String topicName = loggerUpdateTopic.toString().replace("%application.name%",
                ConfigClient.getApplicationName());
        this.loggerUpdateConsumer = session.createConsumer(session.createTopic(topicName));
        this.loggerUpdateConsumer.setMessageListener(loggerUpdateListener);
    }

    private void createSecureStoreUpdateConsumer(Session session, MessageListener secureStoreUpdateListener) throws JMSException {
        String topicName = secureStoreUpdateTopic.toString().replace("%application.name%",
                        ConfigClient.getApplicationName());
        this.secureStoreUpdateConsumer = session.createConsumer(session.createTopic(topicName));
        this.secureStoreUpdateConsumer.setMessageListener(secureStoreUpdateListener);
    }

    void close() {
        if (configUpdateConsumer != null) {
            try {
                configUpdateConsumer.close();
                LOGGER.info("ConfigUpdateConsumer closed.");
            } catch (JMSException e) {
                LOGGER.error("Error in closing config update consumer.", e);
            }
        }

        if (loggerUpdateConsumer != null) {
            try {
                loggerUpdateConsumer.close();
                LOGGER.info("LoggerUpdateConsumer closed");
            } catch (JMSException e) {
                LOGGER.error("Error in closing logger update consumer.", e);
            }
        }

        if (secureStoreUpdateConsumer != null) {
            try {
                secureStoreUpdateConsumer.close();
                LOGGER.info("SecureStoreUpdateConsumer closed");
            } catch (JMSException e) {
                LOGGER.error("Error in closing secure store update consumer.", e);
            }
        }

        if (session != null) {
            try {
                session.close();
                LOGGER.info("Session closed");
            } catch (JMSException e) {
                LOGGER.error("Error in closing the ActiveMQ session.", e);
            }
        }

        if (connection != null) {
            try {
                connection.close();
                LOGGER.info("Connection closed");
            } catch (JMSException e) {
                LOGGER.error("Error in closing the ActiveMQ connection.", e);
            }
        }
    }

}

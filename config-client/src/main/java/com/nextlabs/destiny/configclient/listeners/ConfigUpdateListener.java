package com.nextlabs.destiny.configclient.listeners;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nextlabs.destiny.configclient.ConfigClient;

/**
 * Listen for configuration updates and perform configuration refresh.
 *
 * @author Sachindra Dasun
 */
public class ConfigUpdateListener implements MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUpdateListener.class);
    private static final String CONFIG_UPDATED_MESSAGE = "CONFIG_UPDATED";

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof ActiveMQTextMessage) {
                ActiveMQTextMessage textMessage = (ActiveMQTextMessage) message;
                if (textMessage.getText() != null && textMessage.getText().contains(CONFIG_UPDATED_MESSAGE)) {
                    LOGGER.info("Configuration update message received: {}", textMessage.getText());
                    ConfigClient.refresh();
                }
            }
        } catch (JMSException e) {
            LOGGER.error("Error in getting config update text message", e);
        } catch (Exception e) {
            LOGGER.error("Error in refreshing configurations", e);
        }
    }

}

package com.nextlabs.destiny.configclient.listeners;

import com.nextlabs.destiny.configclient.ConfigClient;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

public class SecureStoreUpdateListener implements MessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecureStoreUpdateListener.class);
    private static final String SECURE_STORE_UPDATED_MESSAGE = "SECURE_STORE_UPDATED";

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof ActiveMQTextMessage) {
                ActiveMQTextMessage textMessage = (ActiveMQTextMessage) message;
                if (textMessage.getText() != null && textMessage.getText().contains(SECURE_STORE_UPDATED_MESSAGE)) {
                    LOGGER.info("Secure store update message received: {}", textMessage.getText());
                    ConfigClient.downloadSecureStore();
                }
            }
        } catch (JMSException e) {
            LOGGER.error("Error in getting secure store update text message", e);
        } catch (Exception e) {
            LOGGER.error("Error in refreshing secure store configurations", e);
        }
    }
}

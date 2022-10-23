package com.nextlabs.destiny.configclient.listeners;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nextlabs.destiny.logmanager.LogManagerClient;

/**
 * Listen for logger configuration updates and perform logger configuration refresh.
 *
 * @author Sachindra Dasun
 */
public class LoggerUpdateListener implements MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerUpdateListener.class);
    private static final String LOGGER_UPDATED_MESSAGE = "LOGGER_UPDATED";

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof ActiveMQTextMessage) {
                ActiveMQTextMessage textMessage = (ActiveMQTextMessage) message;
                if (textMessage.getText() != null && textMessage.getText().contains(LOGGER_UPDATED_MESSAGE)) {
                    LOGGER.info("Logger update message received: {}", textMessage.getText());
                    LogManagerClient.refresh();
                }
            }
        } catch (JMSException e) {
            LOGGER.error("Error in getting logger update text message", e);
        } catch (Exception e) {
            LOGGER.error("Error in refreshing logger configurations", e);
        }
    }

}

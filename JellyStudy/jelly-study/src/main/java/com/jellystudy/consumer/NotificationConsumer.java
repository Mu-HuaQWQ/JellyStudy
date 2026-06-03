package com.jellystudy.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jellystudy.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = "notification.queue")
    public void handleNotification(Map<String, Object> message) {
        try {
            logger.info("Received notification message: {}", message);
            
            String type = (String) message.get("type");
            if ("NEW_NOTIFICATION".equals(type)) {
                String notificationId = (String) message.get("notificationId");
                String userId = (String) message.get("userId");
                
                logger.info("Processing new notification {} for user {}", notificationId, userId);
                
                long unreadCount = notificationService.getUnreadCount(userId);
                logger.info("User {} now has {} unread notifications", userId, unreadCount);
            }
        } catch (Exception e) {
            logger.error("Error processing notification: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "message.queue")
    public void handleMessage(Map<String, Object> message) {
        try {
            logger.info("Received message: {}", message);
            
            String type = (String) message.get("type");
            if ("NEW_MESSAGE".equals(type)) {
                String messageId = (String) message.get("messageId");
                String receiverId = (String) message.get("receiverId");
                String senderName = (String) message.get("senderName");
                
                logger.info("Processing new message {} from {} to user {}", messageId, senderName, receiverId);
            }
        } catch (Exception e) {
            logger.error("Error processing message: {}", e.getMessage(), e);
        }
    }
}
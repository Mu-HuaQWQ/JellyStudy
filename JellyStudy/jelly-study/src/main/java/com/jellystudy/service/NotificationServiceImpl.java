package com.jellystudy.service;

import com.jellystudy.config.NacosConfig;
import com.jellystudy.entity.Notification;
import com.jellystudy.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private NacosConfig nacosConfig;

    @Override
    public Notification createNotification(Notification notification) {
        notification.setCreateTime(LocalDateTime.now());
        notification.setRead(false);
        Notification saved = notificationRepository.save(notification);

        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "NEW_NOTIFICATION");
            message.put("notificationId", saved.getId());
            message.put("userId", saved.getUserId());
            message.put("title", saved.getTitle());
            message.put("content", saved.getContent());

            rabbitTemplate.convertAndSend(
                com.jellystudy.config.RabbitMQConfig.NOTIFICATION_EXCHANGE,
                "notification.new",
                message
            );
        } catch (Exception e) {
            logger.warn("Failed to send notification to RabbitMQ: {}", e.getMessage());
        }

        logger.info("Created notification for user {}: {}", saved.getUserId(), saved.getTitle());
        return saved;
    }

    @Override
    public List<Notification> getUserNotifications(String userId, int page, int size) {
        return notificationRepository.findByUserIdOrderByCreateTimeDesc(userId)
                .stream()
                .skip((long) page * size)
                .limit(size)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public long getUnreadCount(String userId) {
        long count = notificationRepository.countByUserIdAndReadFalse(userId);
        return Math.min(count, nacosConfig.getMaxUnreadCount());
    }

    @Override
    public void markAsRead(String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null && notification.getUserId().equals(userId)) {
            notification.setRead(true);
            notificationRepository.save(notification);
            logger.info("Marked notification {} as read for user {}", notificationId, userId);
        }
    }

    @Override
    public void markAllAsRead(String userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndReadFalseOrderByCreateTimeDesc(userId);
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
        logger.info("Marked all notifications as read for user {}", userId);
    }

    @Override
    public Map<String, Object> getNotificationStats(String userId) {
        Map<String, Object> stats = new HashMap<>();
        long total = notificationRepository.findByUserIdOrderByCreateTimeDesc(userId).size();
        long unread = getUnreadCount(userId);
        stats.put("total", total);
        stats.put("unread", unread);
        stats.put("read", total - unread);
        stats.put("timestamp", System.currentTimeMillis());
        return stats;
    }

    @Override
    public void deleteOldNotifications(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        List<Notification> oldNotifications = notificationRepository.findAll().stream()
                .filter(n -> n.getCreateTime().isBefore(cutoff))
                .collect(java.util.stream.Collectors.toList());

        notificationRepository.deleteAll(oldNotifications);
        logger.info("Deleted {} old notifications older than {} days", oldNotifications.size(), days);
    }
}
package com.jellystudy.service;

import com.jellystudy.entity.Notification;

import java.util.List;
import java.util.Map;

public interface NotificationService {

    Notification createNotification(Notification notification);

    List<Notification> getUserNotifications(String userId, int page, int size);

    long getUnreadCount(String userId);

    void markAsRead(String notificationId, String userId);

    void markAllAsRead(String userId);

    Map<String, Object> getNotificationStats(String userId);

    void deleteOldNotifications(int days);
}
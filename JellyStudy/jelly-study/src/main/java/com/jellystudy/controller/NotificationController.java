package com.jellystudy.controller;

import com.jellystudy.entity.ApiResponse;
import com.jellystudy.entity.Notification;
import com.jellystudy.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping
    public ApiResponse<Notification> createNotification(@RequestBody Notification notification) {
        return ApiResponse.success(notificationService.createNotification(notification));
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<Notification>> getUserNotifications(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(notificationService.getUserNotifications(userId, page, size));
    }

    @GetMapping("/unread/{userId}")
    public ApiResponse<Long> getUnreadCount(@PathVariable String userId) {
        return ApiResponse.success(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{notificationId}/read")
    public ApiResponse<Void> markAsRead(
            @PathVariable String notificationId,
            @RequestParam String userId) {
        notificationService.markAsRead(notificationId, userId);
        return ApiResponse.success();
    }

    @PutMapping("/user/{userId}/read-all")
    public ApiResponse<Void> markAllAsRead(@PathVariable String userId) {
        notificationService.markAllAsRead(userId);
        return ApiResponse.success();
    }

    @GetMapping("/stats/{userId}")
    public ApiResponse<Map<String, Object>> getStats(@PathVariable String userId) {
        return ApiResponse.success(notificationService.getNotificationStats(userId));
    }
}
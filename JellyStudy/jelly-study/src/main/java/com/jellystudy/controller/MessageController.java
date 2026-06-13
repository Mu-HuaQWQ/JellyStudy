package com.jellystudy.controller;

import com.jellystudy.entity.ApiResponse;
import com.jellystudy.entity.Message;
import com.jellystudy.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping
    public ApiResponse<Message> sendMessage(@RequestBody Message message) {
        try {
            Message saved = messageService.sendMessage(message);
            return ApiResponse.success(saved);
        } catch (Exception e) {
            return ApiResponse.error("发送消息失败: " + e.getMessage());
        }
    }

    @GetMapping("/conversation")
    public ApiResponse<List<Message>> getConversation(
            @RequestParam String user1Id,
            @RequestParam String user2Id) {
        return ApiResponse.success(messageService.getConversation(user1Id, user2Id));
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<Message>> getUserMessages(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(messageService.getUserMessages(userId, page, size));
    }

    @GetMapping("/unread/{userId}")
    public ApiResponse<Long> getUnreadCount(@PathVariable String userId) {
        return ApiResponse.success(messageService.getUnreadCount(userId));
    }

    @PutMapping("/{messageId}/read")
    public ApiResponse<Void> markMessageAsRead(
            @PathVariable String messageId,
            @RequestParam String userId) {
        messageService.markMessageAsRead(messageId, userId);
        return ApiResponse.success();
    }

    @PutMapping("/conversation/read-all")
    public ApiResponse<Void> markAllMessagesAsRead(
            @RequestParam String senderId,
            @RequestParam String receiverId) {
        messageService.markAllMessagesAsRead(senderId, receiverId);
        return ApiResponse.success();
    }

    @GetMapping("/stats/{userId}")
    public ApiResponse<Map<String, Object>> getStats(@PathVariable String userId) {
        return ApiResponse.success(messageService.getMessageStats(userId));
    }

    /** 获取联系人列表（含已解析的用户名和头像） */
    @GetMapping("/contacts/{userId}")
    public ApiResponse<List<Map<String, Object>>> getContacts(@PathVariable String userId) {
        return ApiResponse.success(messageService.getContacts(userId));
    }
}
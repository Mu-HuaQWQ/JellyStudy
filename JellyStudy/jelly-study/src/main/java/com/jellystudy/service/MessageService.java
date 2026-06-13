package com.jellystudy.service;

import com.jellystudy.entity.Message;

import java.util.List;
import java.util.Map;

public interface MessageService {

    Message sendMessage(Message message);

    List<Message> getConversation(String user1Id, String user2Id);

    List<Message> getUserMessages(String userId, int page, int size);

    long getUnreadCount(String userId);

    void markMessageAsRead(String messageId, String userId);

    void markAllMessagesAsRead(String senderId, String receiverId);

    Map<String, Object> getMessageStats(String userId);

    List<Map<String, Object>> getContacts(String userId);
}
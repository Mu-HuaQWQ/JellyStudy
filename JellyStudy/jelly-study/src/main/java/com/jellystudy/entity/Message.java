package com.jellystudy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "messages")
public class Message {

    @Id
    private String id;

    private String senderId;
    private String senderName;
    private String receiverId;
    private String content;
    private MessageStatus status;
    private LocalDateTime createTime;
    private LocalDateTime readTime;

    public enum MessageStatus {
        UNREAD,
        READ,
        DELETED
    }
}
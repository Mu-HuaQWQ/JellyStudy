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
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    private String userId;
    private String title;
    private String content;
    private NotificationType type;
    private String relatedId;
    private String relatedType;
    private boolean read;
    private LocalDateTime createTime;

    public enum NotificationType {
        ANSWER("新回答"),
        LIKE("被点赞"),
        COMMENT("新评论"),
        SYSTEM("系统通知");

        private final String description;

        NotificationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
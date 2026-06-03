package com.jellystudy.service;

import com.jellystudy.entity.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationIntegrationService {

    @Autowired
    private NotificationService notificationService;

    public void notifyQuestionAnswered(String questionAuthorId, String questionId, String answerAuthorName) {
        Notification notification = Notification.builder()
                .userId(questionAuthorId)
                .title("你的问题有了新回答")
                .content("用户 " + answerAuthorName + " 回答了你的问题")
                .type(Notification.NotificationType.ANSWER)
                .relatedId(questionId)
                .relatedType("question")
                .build();

        notificationService.createNotification(notification);
    }

    public void notifyAnswerLiked(String answerAuthorId, String answerId, String likerName) {
        Notification notification = Notification.builder()
                .userId(answerAuthorId)
                .title("你的回答被点赞")
                .content("用户 " + likerName + " 点赞了你的回答")
                .type(Notification.NotificationType.LIKE)
                .relatedId(answerId)
                .relatedType("answer")
                .build();

        notificationService.createNotification(notification);
    }

    public void notifyQuestionLiked(String questionAuthorId, String questionId, String likerName) {
        Notification notification = Notification.builder()
                .userId(questionAuthorId)
                .title("你的问题被点赞")
                .content("用户 " + likerName + " 点赞了你的问题")
                .type(Notification.NotificationType.LIKE)
                .relatedId(questionId)
                .relatedType("question")
                .build();

        notificationService.createNotification(notification);
    }
}
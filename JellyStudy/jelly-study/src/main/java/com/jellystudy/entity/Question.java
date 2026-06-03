package com.jellystudy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "questions")
public class Question {
    
    @Id
    private String id;
    
    private String title;
    
    private String content;
    
    private String authorId;
    
    private String authorName;
    
    private String knowledgePointId;
    
    private String knowledgePointTitle;
    
    private List<String> tags;
    
    private QuestionStatus status;
    
    private Integer viewCount;
    
    private Integer answerCount;
    
    private Set<String> likedByUsers;
    
    private Integer likeCount;
    
    private Boolean isDeleted;
    
    private String deletedReason;
    
    @CreatedDate
    private LocalDateTime createTime;
    
    @LastModifiedDate
    private LocalDateTime updateTime;
    
    private LocalDateTime lastAnswerTime;
    
    @Builder.Default
    private List<Answer> answers = new ArrayList<>();
    
    public enum QuestionStatus {
        PENDING,
        ANSWERED,
        CLOSED,
        DELETED
    }
}

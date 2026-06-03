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
@Document(collection = "answers")
public class Answer {
    
    @Id
    private String id;
    
    private String questionId;
    
    private String content;
    
    private String authorId;
    
    private String authorName;
    
    private Boolean isAccepted;
    
    private Set<String> likedByUsers;
    
    private Integer likeCount;
    
    private Boolean isDeleted;
    
    private String deletedReason;
    
    @CreatedDate
    private LocalDateTime createTime;
    
    @LastModifiedDate
    private LocalDateTime updateTime;
    
    private Integer commentCount;
    
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();
    
    public enum AnswerStatus {
        ACTIVE,
        DELETED
    }
}

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
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "comments")
public class Comment {
    
    @Id
    private String id;
    
    private String targetId;
    
    private CommentType commentType;
    
    private String parentCommentId;
    
    private String content;
    
    private String authorId;
    
    private String authorName;
    
    private Set<String> likedByUsers;
    
    private Integer likeCount;
    
    private Boolean isDeleted;
    
    @CreatedDate
    private LocalDateTime createTime;
    
    @LastModifiedDate
    private LocalDateTime updateTime;
    
    public enum CommentType {
        QUESTION,
        ANSWER,
        COMMENT
    }
}

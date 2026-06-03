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
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "knowledge_points")
public class KnowledgePoint {
    
    @Id
    private String id;
    
    private String title;
    
    private String content;
    
    private String category;
    
    private List<String> tags;
    
    private String difficulty;
    
    private String authorId;
    
    private String authorName;
    
    @CreatedDate
    private LocalDateTime createTime;
    
    @LastModifiedDate
    private LocalDateTime updateTime;
    
    private Integer viewCount;
    
    private Integer questionCount;
    
    private Boolean isPublished;
    
    private String parentId;
    
    private String path;
}

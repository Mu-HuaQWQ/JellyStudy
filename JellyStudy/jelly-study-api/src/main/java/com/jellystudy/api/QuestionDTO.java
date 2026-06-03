package com.jellystudy.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String title;
    private String content;
    private String authorId;
    private String authorName;
    private String knowledgePointId;
    private String knowledgePointTitle;
    private Integer viewCount;
    private Integer answerCount;
    private Integer likeCount;
    private List<String> tags;
    private Boolean isDeleted;
}

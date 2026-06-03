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
public class AnswerDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String questionId;
    private String authorId;
    private String authorName;
    private String content;
    private Integer likeCount;
    private Boolean isAccepted;
    private Boolean isDeleted;
    private String deletedReason;
}

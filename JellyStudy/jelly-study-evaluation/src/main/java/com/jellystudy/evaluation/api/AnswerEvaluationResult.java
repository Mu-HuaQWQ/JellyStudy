package com.jellystudy.evaluation.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerEvaluationResult implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String id;
    
    private String answerId;
    
    private String questionId;
    
    private Integer score;
    
    private Double scoreOutOf5;
    
    private String feedback;
    
    private String evaluationReason;
    
    private String evaluationCriteria;
    
    private Double confidenceScore;
    
    private boolean success;
    
    private String errorMessage;
}

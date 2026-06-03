package com.jellystudy.evaluation.api;

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
public class QuestionEvaluationResult implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String id;
    
    private String questionId;
    
    private List<String> extractedKnowledgePoints;
    
    private String difficultyLevel;
    
    private String difficultyReason;
    
    private Double confidenceScore;
    
    private boolean success;
    
    private String errorMessage;
}

package com.jellystudy.evaluation.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "question_evaluations")
public class QuestionEvaluation {
    
    @Id
    private String id;
    
    private String questionId;
    
    private String questionTitle;
    
    private String questionContent;
    
    private List<String> extractedKnowledgePoints;
    
    private DifficultyLevel difficultyLevel;
    
    private String difficultyReason;
    
    private Double confidenceScore;
    
    @CreatedDate
    private LocalDateTime createTime;
    
    public enum DifficultyLevel {
        EASY,
        MEDIUM,
        HARD
    }
}

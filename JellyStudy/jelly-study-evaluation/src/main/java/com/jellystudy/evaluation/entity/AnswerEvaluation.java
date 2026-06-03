package com.jellystudy.evaluation.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "answer_evaluations")
public class AnswerEvaluation {
    
    @Id
    private String id;
    
    private String answerId;
    
    private String questionId;
    
    private String answerContent;
    
    private Integer score;
    
    private Integer scoreOutOf100;
    
    private Double scoreOutOf5;
    
    private String feedback;
    
    private String evaluationReason;
    
    private String evaluationCriteria;
    
    private Double confidenceScore;
    
    @CreatedDate
    private LocalDateTime createTime;
}

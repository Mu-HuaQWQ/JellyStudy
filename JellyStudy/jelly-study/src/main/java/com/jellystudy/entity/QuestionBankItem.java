package com.jellystudy.entity;

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
@Document(collection = "question_bank_items")
public class QuestionBankItem {

    @Id
    private String id;

    private String knowledgePointId;

    private String knowledgePointTitle;

    private String type;  // "CHOICE" 或 "TF"

    private String question;

    private List<String> options;

    private String correctAnswer;

    private String explanation;

    private String difficulty;  // "简单" / "中等" / "困难"

    private String authorId;    // "ai" 或用户ID

    @CreatedDate
    private LocalDateTime createTime;
}

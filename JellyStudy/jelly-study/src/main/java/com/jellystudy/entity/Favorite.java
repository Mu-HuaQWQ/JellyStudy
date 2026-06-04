package com.jellystudy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "favorites")
@CompoundIndex(name = "user_question_idx", def = "{'userId': 1, 'questionId': 1}", unique = true)
public class Favorite {

    @Id
    private String id;

    private String userId;

    private String questionId;

    // 问题标题（冗余存储，便于列表展示）
    private String questionTitle;

    @CreatedDate
    private LocalDateTime createTime;
}

package com.jellystudy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "browse_history")
@CompoundIndex(name = "user_question_idx", def = "{'userId': 1, 'questionId': 1}", unique = true)
public class BrowseHistory {

    @Id
    private String id;

    private String userId;

    private String questionId;

    // 问题标题（冗余存储，便于列表展示）
    private String questionTitle;

    // 最近浏览时间（同问题重复浏览时更新）
    private LocalDateTime viewTime;
}

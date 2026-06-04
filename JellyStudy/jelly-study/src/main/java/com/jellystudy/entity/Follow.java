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
@Document(collection = "follows")
@CompoundIndex(name = "follower_following_idx", def = "{'followerId': 1, 'followingId': 1}", unique = true)
public class Follow {

    @Id
    private String id;

    // 关注者
    private String followerId;

    // 被关注者
    private String followingId;

    @CreatedDate
    private LocalDateTime createTime;
}

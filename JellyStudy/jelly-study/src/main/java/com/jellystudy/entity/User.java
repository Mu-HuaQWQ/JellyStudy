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
@Document(collection = "users")
public class User {
    
    @Id
    private String id;
    
    private String username;
    
    private String password;
    
    private String nickname;
    
    private String email;
    
    private String avatar;
    
    private String role;
    
    private Integer reputation;

    private Integer questionCount;

    private Integer answerCount;

    // 关注数（冗余存储）
    private Integer followingCount;

    // 粉丝数（冗余存储）
    private Integer followerCount;

    private Integer creditPoints;   // 信用点余额，默认0

    private Integer totalSpent;     // 累计消耗信用点，默认0

    private Integer level;          // 用户等级 0-6，默认0

    // 已解锁的称号 code
    private List<String> ownedTitles;

    // 当前佩戴展示的称号 code
    private String displayTitle;

    @CreatedDate
    private LocalDateTime createTime;
}

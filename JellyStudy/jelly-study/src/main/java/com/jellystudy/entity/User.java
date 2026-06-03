package com.jellystudy.entity;

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
    
    @CreatedDate
    private LocalDateTime createTime;
}

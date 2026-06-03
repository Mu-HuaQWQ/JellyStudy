package com.jellystudy.entity;

import com.jellystudy.entity.Comment.CommentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {
    
    @NotNull(message = "目标ID不能为空")
    private String targetId;
    
    @NotNull(message = "评论类型不能为空")
    private CommentType commentType;
    
    private String parentCommentId;
    
    @NotBlank(message = "评论内容不能为空")
    private String content;
}

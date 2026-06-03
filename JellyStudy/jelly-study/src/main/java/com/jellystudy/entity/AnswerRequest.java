package com.jellystudy.entity;

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
public class AnswerRequest {
    
    @NotNull(message = "问题ID不能为空")
    private String questionId;
    
    @NotBlank(message = "回答内容不能为空")
    private String content;
}

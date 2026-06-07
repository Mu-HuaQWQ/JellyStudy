package com.jellystudy.controller;

import com.jellystudy.entity.ApiResponse;
import com.jellystudy.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    @Autowired
    private AIService aiService;

    @PostMapping("/answer")
    public ApiResponse<String> answerQuestion(@RequestBody Map<String, String> request) {
        String questionTitle = request.get("questionTitle");
        String questionContent = request.get("questionContent");
        String persona = request.get("persona"); // 可选，null 则用默认老师

        if (questionTitle == null || questionTitle.trim().isEmpty()) {
            return ApiResponse.error(400, "问题标题不能为空");
        }

        String answer = aiService.answerQuestion(
            questionTitle,
            questionContent != null ? questionContent : "",
            persona
        );
        return ApiResponse.success(answer);
    }
}

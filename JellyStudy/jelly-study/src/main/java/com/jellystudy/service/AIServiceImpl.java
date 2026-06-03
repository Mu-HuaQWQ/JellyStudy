package com.jellystudy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AIServiceImpl implements AIService {

    @Value("${ai.deepseek.api-key}")
    private String apiKey;

    @Value("${ai.deepseek.base-url}")
    private String baseUrl;

    @Value("${ai.deepseek.model}")
    private String model;

    @Override
    public String answerQuestion(String questionTitle, String questionContent) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            String prompt = "你是一个知识渊博的老师，请根据以下问题给出详细、准确的回答。\n\n" +
                    "问题标题：" + questionTitle + "\n" +
                    "问题内容：" + questionContent + "\n\n" +
                    "请给出回答：";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", Arrays.asList(
                Map.of("role", "system", "content", "你是一个知识渊博的老师，善于用通俗易懂的语言解答问题。"),
                Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("temperature", 0.7);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/v1/chat/completions",
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
            
            return "AI暂时无法回答此问题，请稍后再试。";
            
        } catch (Exception e) {
            e.printStackTrace();
            return "调用AI服务失败：" + e.getMessage();
        }
    }
}

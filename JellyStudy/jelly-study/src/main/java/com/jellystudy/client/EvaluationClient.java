package com.jellystudy.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class EvaluationClient {
    
    private static final Logger logger = LoggerFactory.getLogger(EvaluationClient.class);
    
    private final RestTemplate restTemplate;
    private final ExecutorService executorService;
    
    @Value("${evaluation.service.url:http://localhost:8087}")
    private String evaluationServiceUrl;

    public EvaluationClient() {
        this.restTemplate = new RestTemplate();
        this.executorService = Executors.newFixedThreadPool(2);
    }

    public CompletableFuture<Map<String, Object>> evaluateQuestionAsync(String questionId, String title, String content) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                
                Map<String, String> requestBody = Map.of(
                    "questionId", questionId != null ? questionId : "",
                    "questionTitle", title != null ? title : "",
                    "questionContent", content != null ? content : ""
                );
                
                HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
                
                String url = evaluationServiceUrl + "/api/evaluation/question/evaluate";
                logger.info("Calling evaluation service: {}", url);
                
                ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Map.class
                );
                
                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    logger.info("Question evaluation completed for: {}", questionId);
                    return response.getBody();
                }
                
                logger.warn("Evaluation service returned: {}", response.getStatusCode());
                return Map.of("success", false, "error", "Evaluation service error");
                
            } catch (Exception e) {
                logger.error("Failed to evaluate question: {}", questionId, e);
                return Map.of("success", false, "error", e.getMessage());
            }
        }, executorService);
    }

    public CompletableFuture<Map<String, Object>> evaluateAnswerAsync(String answerId, String questionTitle, String questionContent, String answerContent) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                
                Map<String, String> requestBody = Map.of(
                    "answerId", answerId != null ? answerId : "",
                    "questionId", "",
                    "questionContent", questionTitle + "\n" + (questionContent != null ? questionContent : ""),
                    "answerContent", answerContent != null ? answerContent : ""
                );
                
                HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
                
                String url = evaluationServiceUrl + "/api/evaluation/answer/evaluate";
                logger.info("Calling evaluation service: {}", url);
                
                ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Map.class
                );
                
                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    logger.info("Answer evaluation completed for: {}", answerId);
                    return response.getBody();
                }
                
                logger.warn("Evaluation service returned: {}", response.getStatusCode());
                return Map.of("success", false, "error", "Evaluation service error");
                
            } catch (Exception e) {
                logger.error("Failed to evaluate answer: {}", answerId, e);
                return Map.of("success", false, "error", e.getMessage());
            }
        }, executorService);
    }

    public boolean isEvaluationServiceAvailable() {
        try {
            String url = evaluationServiceUrl + "/actuator/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            logger.debug("Evaluation service not available: {}", e.getMessage());
            return false;
        }
    }
}

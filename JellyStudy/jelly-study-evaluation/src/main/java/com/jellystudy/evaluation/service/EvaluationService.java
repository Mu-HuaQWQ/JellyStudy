package com.jellystudy.evaluation.service;

import com.jellystudy.evaluation.api.AnswerEvaluationResult;
import com.jellystudy.evaluation.api.QuestionEvaluationResult;
import com.jellystudy.evaluation.entity.AnswerEvaluation;
import com.jellystudy.evaluation.entity.QuestionEvaluation;
import com.jellystudy.evaluation.entity.QuestionEvaluation.DifficultyLevel;
import com.jellystudy.evaluation.repository.AnswerEvaluationRepository;
import com.jellystudy.evaluation.repository.QuestionEvaluationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class EvaluationService {
    
    @Value("${ai.deepseek.api-key}")
    private String apiKey;

    @Value("${ai.deepseek.base-url}")
    private String baseUrl;

    @Value("${ai.deepseek.model}")
    private String model;
    
    private final QuestionEvaluationRepository questionEvaluationRepository;
    private final AnswerEvaluationRepository answerEvaluationRepository;
    
    public EvaluationService(QuestionEvaluationRepository questionEvaluationRepository,
                           AnswerEvaluationRepository answerEvaluationRepository) {
        this.questionEvaluationRepository = questionEvaluationRepository;
        this.answerEvaluationRepository = answerEvaluationRepository;
    }
    
    public QuestionEvaluationResult evaluateQuestion(String questionId, String questionTitle, String questionContent) {
        try {
            String prompt = buildQuestionEvaluationPrompt(questionTitle, questionContent);
            
            String llmResponse = callLLM(prompt);
            
            QuestionEvaluationResult result = parseQuestionEvaluationResponse(llmResponse);
            result.setQuestionId(questionId);
            
            QuestionEvaluation evaluation = QuestionEvaluation.builder()
                    .questionId(questionId)
                    .questionTitle(questionTitle)
                    .questionContent(questionContent)
                    .extractedKnowledgePoints(result.getExtractedKnowledgePoints())
                    .difficultyLevel(DifficultyLevel.valueOf(result.getDifficultyLevel()))
                    .difficultyReason(result.getDifficultyReason())
                    .confidenceScore(result.getConfidenceScore())
                    .build();
            
            QuestionEvaluation saved = questionEvaluationRepository.save(evaluation);
            result.setId(saved.getId());
            result.setSuccess(true);
            
            return result;
            
        } catch (Exception e) {
            e.printStackTrace();
            return QuestionEvaluationResult.builder()
                    .questionId(questionId)
                    .success(false)
                    .errorMessage("Evaluation failed: " + e.getMessage())
                    .build();
        }
    }
    
    public AnswerEvaluationResult evaluateAnswer(String answerId, String questionId, 
                                                 String questionContent, String answerContent) {
        try {
            String prompt = buildAnswerEvaluationPrompt(questionContent, answerContent);
            
            String llmResponse = callLLM(prompt);
            
            AnswerEvaluationResult result = parseAnswerEvaluationResponse(llmResponse);
            result.setAnswerId(answerId);
            result.setQuestionId(questionId);
            
            AnswerEvaluation evaluation = AnswerEvaluation.builder()
                    .answerId(answerId)
                    .questionId(questionId)
                    .answerContent(answerContent)
                    .score(result.getScore())
                    .scoreOutOf100(result.getScore())
                    .scoreOutOf5(result.getScoreOutOf5())
                    .feedback(result.getFeedback())
                    .evaluationReason(result.getEvaluationReason())
                    .evaluationCriteria(result.getEvaluationCriteria())
                    .confidenceScore(result.getConfidenceScore())
                    .build();
            
            AnswerEvaluation saved = answerEvaluationRepository.save(evaluation);
            result.setId(saved.getId());
            result.setSuccess(true);
            
            return result;
            
        } catch (Exception e) {
            e.printStackTrace();
            return AnswerEvaluationResult.builder()
                    .answerId(answerId)
                    .questionId(questionId)
                    .success(false)
                    .errorMessage("Evaluation failed: " + e.getMessage())
                    .build();
        }
    }
    
    public QuestionEvaluationResult getQuestionEvaluation(String questionId) {
        return questionEvaluationRepository.findByQuestionId(questionId)
                .map(e -> QuestionEvaluationResult.builder()
                        .id(e.getId())
                        .questionId(e.getQuestionId())
                        .extractedKnowledgePoints(e.getExtractedKnowledgePoints())
                        .difficultyLevel(e.getDifficultyLevel().name())
                        .difficultyReason(e.getDifficultyReason())
                        .confidenceScore(e.getConfidenceScore())
                        .success(true)
                        .build())
                .orElse(QuestionEvaluationResult.builder()
                        .questionId(questionId)
                        .success(false)
                        .errorMessage("Evaluation not found")
                        .build());
    }
    
    public AnswerEvaluationResult getAnswerEvaluation(String answerId) {
        try {
            return answerEvaluationRepository.findFirstByAnswerIdOrderByCreateTimeDesc(answerId)
                    .map(e -> AnswerEvaluationResult.builder()
                            .id(e.getId())
                            .answerId(e.getAnswerId())
                            .questionId(e.getQuestionId())
                            .score(e.getScore())
                            .scoreOutOf5(e.getScoreOutOf5())
                            .feedback(e.getFeedback())
                            .evaluationReason(e.getEvaluationReason())
                            .evaluationCriteria(e.getEvaluationCriteria())
                            .confidenceScore(e.getConfidenceScore())
                            .success(true)
                            .build())
                    .orElse(AnswerEvaluationResult.builder()
                            .answerId(answerId)
                            .success(false)
                            .errorMessage("Evaluation not found")
                            .build());
        } catch (Exception ex) {
            ex.printStackTrace();
            return AnswerEvaluationResult.builder()
                    .answerId(answerId)
                    .success(false)
                    .errorMessage("Error: " + ex.getMessage())
                    .build();
        }
    }
    
    public List<QuestionEvaluationResult> getQuestionEvaluations(List<String> questionIds) {
        return questionEvaluationRepository.findByQuestionIdIn(questionIds).stream()
                .map(e -> QuestionEvaluationResult.builder()
                        .id(e.getId())
                        .questionId(e.getQuestionId())
                        .extractedKnowledgePoints(e.getExtractedKnowledgePoints())
                        .difficultyLevel(e.getDifficultyLevel().name())
                        .difficultyReason(e.getDifficultyReason())
                        .confidenceScore(e.getConfidenceScore())
                        .success(true)
                        .build())
                .collect(Collectors.toList());
    }
    
    public List<AnswerEvaluationResult> getAnswerEvaluations(List<String> answerIds) {
        return answerEvaluationRepository.findByAnswerIdIn(answerIds).stream()
                .map(e -> AnswerEvaluationResult.builder()
                        .id(e.getId())
                        .answerId(e.getAnswerId())
                        .questionId(e.getQuestionId())
                        .score(e.getScore())
                        .scoreOutOf5(e.getScoreOutOf5())
                        .feedback(e.getFeedback())
                        .evaluationCriteria(e.getEvaluationCriteria())
                        .confidenceScore(e.getConfidenceScore())
                        .success(true)
                        .build())
                .collect(Collectors.toList());
    }
    
    private String buildQuestionEvaluationPrompt(String title, String content) {
        return "你是一个教育专家，请分析以下问题并提取知识点和评估难度。\n\n" +
                "问题标题：" + title + "\n" +
                "问题内容：" + content + "\n\n" +
                "请按照以下JSON格式返回分析结果（只返回JSON，不要其他内容）：\n" +
                "{\n" +
                "  \"knowledgePoints\": [\"知识点1\", \"知识点2\"],\n" +
                "  \"difficulty\": \"EASY/MEDIUM/HARD\",\n" +
                "  \"reason\": \"难度评估理由\",\n" +
                "  \"confidence\": 0.85\n" +
                "}\n\n" +
                "注意：\n" +
                "- difficulty只能取EASY、MEDIUM或HARD\n" +
                "- confidence为0-1之间的置信度分数\n" +
                "- knowledgePoints请列出问题的核心知识点";
    }
    
    private String buildAnswerEvaluationPrompt(String questionContent, String answerContent) {
        return "你是一个答题评估专家，请根据问题内容和参考答案（如果有）评估以下答案的质量。\n\n" +
                "问题内容：" + questionContent + "\n\n" +
                "用户答案：" + answerContent + "\n\n" +
                "请按照以下JSON格式返回评估结果（只返回JSON，不要其他内容）：\n" +
                "{\n" +
                "  \"score\": 85,\n" +
                "  \"scoreOutOf5\": 4.25,\n" +
                "  \"feedback\": \"评价反馈文字\",\n" +
                "  \"criteria\": \"评估标准说明\",\n" +
                "  \"confidence\": 0.9\n" +
                "}\n\n" +
                "注意：\n" +
                "- score为0-100的分数\n" +
                "- scoreOutOf5为1-5的分数\n" +
                "- feedback给出具体的优缺点和改进建议\n" +
                "- confidence为0-1之间的置信度分数";
    }
    
    private String callLLM(String prompt) {
        RestTemplate restTemplate = new RestTemplate();
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", Arrays.asList(
            Map.of("role", "system", "content", "你是一个专业的教育评估助手，擅长分析问题和评估答案。请严格按照要求的JSON格式返回结果。"),
            Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("temperature", 0.3);
        
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
        
        throw new RuntimeException("LLM调用失败");
    }
    
    @SuppressWarnings("unchecked")
    private QuestionEvaluationResult parseQuestionEvaluationResponse(String response) {
        try {
            Pattern pattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(response);
            
            if (matcher.find()) {
                String jsonStr = matcher.group();
                Map<String, Object> json = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(jsonStr, Map.class);
                
                List<String> knowledgePoints = new ArrayList<>();
                if (json.get("knowledgePoints") instanceof List) {
                    knowledgePoints = (List<String>) json.get("knowledgePoints");
                }
                
                String difficulty = (String) json.getOrDefault("difficulty", "MEDIUM");
                String reason = (String) json.getOrDefault("reason", "");
                Double confidence = json.get("confidence") != null ? 
                    ((Number) json.get("confidence")).doubleValue() : 0.8;
                
                return QuestionEvaluationResult.builder()
                        .extractedKnowledgePoints(knowledgePoints)
                        .difficultyLevel(difficulty)
                        .difficultyReason(reason)
                        .confidenceScore(confidence)
                        .success(true)
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return QuestionEvaluationResult.builder()
                .extractedKnowledgePoints(new ArrayList<>())
                .difficultyLevel("MEDIUM")
                .difficultyReason("解析失败，使用默认中等难度")
                .confidenceScore(0.5)
                .success(true)
                .build();
    }
    
    @SuppressWarnings("unchecked")
    private AnswerEvaluationResult parseAnswerEvaluationResponse(String response) {
        try {
            Pattern pattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(response);
            
            if (matcher.find()) {
                String jsonStr = matcher.group();
                Map<String, Object> json = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(jsonStr, Map.class);
                
                Integer score = json.get("score") != null ? 
                    ((Number) json.get("score")).intValue() : 75;
                Double scoreOutOf5 = json.get("scoreOutOf5") != null ? 
                    ((Number) json.get("scoreOutOf5")).doubleValue() : 3.75;
                String feedback = (String) json.getOrDefault("feedback", "");
                String criteria = (String) json.getOrDefault("criteria", "");
                Double confidence = json.get("confidence") != null ? 
                    ((Number) json.get("confidence")).doubleValue() : 0.8;
                
                return AnswerEvaluationResult.builder()
                        .score(score)
                        .scoreOutOf5(scoreOutOf5)
                        .feedback(feedback)
                        .evaluationReason(feedback)
                        .evaluationCriteria(criteria)
                        .confidenceScore(confidence)
                        .success(true)
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return AnswerEvaluationResult.builder()
                .score(75)
                .scoreOutOf5(3.75)
                .feedback("评估解析失败，使用默认分数")
                .evaluationReason("评估解析失败，使用默认分数")
                .evaluationCriteria("默认评估标准")
                .confidenceScore(0.5)
                .success(true)
                .build();
    }
}

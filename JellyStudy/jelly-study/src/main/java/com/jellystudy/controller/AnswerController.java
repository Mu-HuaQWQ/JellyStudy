package com.jellystudy.controller;

import com.jellystudy.entity.*;
import com.jellystudy.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/answers")
@CrossOrigin(origins = "*")
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    @Autowired
    private QuestionService questionService;

    @Value("${evaluation.service.url:http://host.docker.internal:8089}")
    private String evaluationServiceUrl;

    @PostMapping
    public ApiResponse<Answer> createAnswer(
            @RequestBody @Validated AnswerRequest answerRequest,
            @RequestParam(defaultValue = "user001") String authorId,
            @RequestParam(defaultValue = "Anonymous") String authorName) {
        Answer answer = answerService.create(answerRequest, authorId, authorName);
        return ApiResponse.success(answer);
    }

    @GetMapping("/{id}")
    public ApiResponse<Answer> getAnswer(@PathVariable String id) {
        return answerService.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "Answer not found"));
    }

    @GetMapping("/{id}/evaluation")
    public ApiResponse<Map<String, Object>> getAnswerEvaluation(@PathVariable String id) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = evaluationServiceUrl + "/api/evaluation/answer/" + id;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return ApiResponse.success(response.getBody());
            }
            return ApiResponse.error("Evaluation not found");
        } catch (Exception e) {
            return ApiResponse.error("Failed to get evaluation: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/re-evaluate")
    public ApiResponse<Map<String, Object>> reevaluateAnswer(@PathVariable String id, @RequestBody Map<String, String> request) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = evaluationServiceUrl + "/api/evaluation/answer/evaluate";
            request.put("answerId", id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            org.springframework.http.HttpEntity<Map<String, String>> entity = new org.springframework.http.HttpEntity<>(request, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return ApiResponse.success(response.getBody());
            }
            return ApiResponse.error("Re-evaluation failed");
        } catch (Exception e) {
            return ApiResponse.error("Failed to re-evaluate: " + e.getMessage());
        }
    }

    @GetMapping("/question/{questionId}")
    public ApiResponse<List<Answer>> getAnswersByQuestion(@PathVariable String questionId) {
        List<Answer> answers = answerService.findByQuestionId(questionId);
        return ApiResponse.success(answers);
    }

    @GetMapping("/author/{authorId}")
    public ApiResponse<List<Answer>> getAnswersByAuthor(@PathVariable String authorId) {
        List<Answer> answers = answerService.findByAuthorId(authorId);
        return ApiResponse.success(answers);
    }

    @GetMapping("/hot")
    public ApiResponse<List<Answer>> getHotAnswers(
            @RequestParam(defaultValue = "10") int limit) {
        List<Answer> answers = answerService.findHotAnswers(limit);
        return ApiResponse.success(answers);
    }

    @GetMapping("/top-liked/{questionId}")
    public ApiResponse<List<Answer>> getTopLikedAnswers(
            @PathVariable String questionId,
            @RequestParam(defaultValue = "10") int limit) {
        List<Answer> answers = answerService.findTopLikedAnswers(questionId, limit);
        return ApiResponse.success(answers);
    }

    @GetMapping("/accepted/{questionId}")
    public ApiResponse<Answer> getAcceptedAnswer(@PathVariable String questionId) {
        Answer answer = answerService.findAcceptedAnswer(questionId);
        return ApiResponse.success(answer);
    }

    @GetMapping("/count")
    public ApiResponse<Long> getAnswerCount() {
        Long count = answerService.count();
        return ApiResponse.success(count);
    }

    @PutMapping("/{id}")
    public ApiResponse<Answer> updateAnswer(
            @PathVariable String id,
            @RequestBody Answer answer) {
        try {
            Answer updated = answerService.update(id, answer);
            return ApiResponse.success(updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAnswer(
            @PathVariable String id,
            @RequestParam String userId) {
        try {
            answerService.delete(id, userId);
            return ApiResponse.success();
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/accept")
    public ApiResponse<Answer> acceptAnswer(
            @PathVariable String id,
            @RequestParam(defaultValue = "user001") String userId) {
        try {
            Answer answer = answerService.acceptAnswer(id, userId);
            return ApiResponse.success(answer);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/like")
    public ApiResponse<Answer> likeAnswer(
            @PathVariable String id,
            @RequestParam(defaultValue = "user001") String userId) {
        try {
            Answer answer = answerService.likeAnswer(id, userId);
            return ApiResponse.success(answer);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/unlike")
    public ApiResponse<Answer> unlikeAnswer(
            @PathVariable String id,
            @RequestParam(defaultValue = "user001") String userId) {
        try {
            Answer answer = answerService.unlikeAnswer(id, userId);
            return ApiResponse.success(answer);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}

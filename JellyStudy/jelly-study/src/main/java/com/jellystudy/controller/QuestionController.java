package com.jellystudy.controller;

import com.jellystudy.client.EvaluationClient;
import com.jellystudy.entity.*;
import com.jellystudy.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "*")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerService answerService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private KnowledgePointService knowledgePointService;

    @Autowired
    private EvaluationClient evaluationClient;

    @Value("${evaluation.service.url:http://host.docker.internal:8089}")
    private String evaluationServiceUrl;

    @PostMapping
    public ApiResponse<Question> createQuestion(
            @RequestBody @Validated QuestionRequest questionRequest,
            @RequestParam(defaultValue = "user001") String authorId,
            @RequestParam(defaultValue = "Anonymous") String authorName) {
        Question question = questionService.create(questionRequest, authorId, authorName);
        return ApiResponse.success(question);
    }

    @GetMapping("/{id}")
    public ApiResponse<Question> getQuestion(@PathVariable String id) {
        questionService.incrementViewCount(id);
        return questionService.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "Question not found"));
    }

    @GetMapping("/{id}/evaluation")
    public ApiResponse<Map<String, Object>> getQuestionEvaluation(@PathVariable String id) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = evaluationServiceUrl + "/api/evaluation/question/" + id;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return ApiResponse.success(response.getBody());
            }
            return ApiResponse.error("Evaluation not found");
        } catch (Exception e) {
            return ApiResponse.error("Failed to get evaluation: " + e.getMessage());
        }
    }

    @GetMapping
    public ApiResponse<Page<Question>> getAllQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Question> questions = questionService.findAll(page, size);
        return ApiResponse.success(questions);
    }

    @GetMapping("/search")
    public ApiResponse<List<Question>> searchQuestions(@RequestParam String keyword) {
        List<Question> questions = questionService.search(keyword);
        return ApiResponse.success(questions);
    }

    @GetMapping("/hot")
    public ApiResponse<List<Question>> getHotQuestions(
            @RequestParam(defaultValue = "10") int limit) {
        List<Question> questions = questionService.findHotQuestions(limit);
        return ApiResponse.success(questions);
    }

    @GetMapping("/top-viewed")
    public ApiResponse<List<Question>> getTopViewedQuestions(
            @RequestParam(defaultValue = "10") int limit) {
        List<Question> questions = questionService.findTopViewed(limit);
        return ApiResponse.success(questions);
    }

    @GetMapping("/recommend")
    public ApiResponse<List<Question>> getRecommendQuestions(
            @RequestParam(defaultValue = "10") int limit) {
        List<Question> questions = questionService.getRecommendQuestions(limit);
        return ApiResponse.success(questions);
    }

    @GetMapping("/knowledge-point/{knowledgePointId}")
    public ApiResponse<List<Question>> getQuestionsByKnowledgePoint(
            @PathVariable String knowledgePointId) {
        List<Question> questions = questionService.findByKnowledgePointId(knowledgePointId);
        return ApiResponse.success(questions);
    }

    @GetMapping("/author/{authorId}")
    public ApiResponse<List<Question>> getQuestionsByAuthor(@PathVariable String authorId) {
        List<Question> questions = questionService.findByAuthorId(authorId);
        return ApiResponse.success(questions);
    }

    @GetMapping("/count")
    public ApiResponse<Long> getQuestionCount() {
        Long count = questionService.count();
        return ApiResponse.success(count);
    }

    @PutMapping("/{id}")
    public ApiResponse<Question> updateQuestion(
            @PathVariable String id,
            @RequestBody Question question) {
        try {
            Question updated = questionService.update(id, question);
            return ApiResponse.success(updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteQuestion(
            @PathVariable String id,
            @RequestParam String userId) {
        try {
            questionService.delete(id, userId);
            return ApiResponse.success();
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/like")
    public ApiResponse<Question> likeQuestion(
            @PathVariable String id,
            @RequestParam(defaultValue = "user001") String userId) {
        try {
            Question question = questionService.likeQuestion(id, userId);
            return ApiResponse.success(question);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/unlike")
    public ApiResponse<Question> unlikeQuestion(
            @PathVariable String id,
            @RequestParam(defaultValue = "user001") String userId) {
        try {
            Question question = questionService.unlikeQuestion(id, userId);
            return ApiResponse.success(question);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}/detail")
    public ApiResponse<Map<String, Object>> getQuestionDetail(@PathVariable String id) {
        Map<String, Object> detail = new HashMap<>();
        
        Question question = questionService.findById(id).orElse(null);
        if (question == null) {
            return ApiResponse.error(404, "Question not found");
        }
        
        questionService.incrementViewCount(id);
        question = questionService.findById(id).orElse(question);
        
        List<Answer> answers = answerService.findByQuestionId(id);
        Answer acceptedAnswer = answerService.findAcceptedAnswer(id);
        
        List<Comment> questionComments = commentService.findByTargetIdAndType(
                id, Comment.CommentType.QUESTION);
        
        Map<String, List<Comment>> answerCommentsMap = new HashMap<>();
        for (Answer answer : answers) {
            List<Comment> answerComments = commentService.findByTargetIdAndType(
                    answer.getId(), Comment.CommentType.ANSWER);
            answerCommentsMap.put(answer.getId(), answerComments);
        }
        
        detail.put("question", question);
        detail.put("answers", answers);
        detail.put("acceptedAnswer", acceptedAnswer);
        detail.put("questionComments", questionComments);
        detail.put("answerCommentsMap", answerCommentsMap);
        
        return ApiResponse.success(detail);
    }
}

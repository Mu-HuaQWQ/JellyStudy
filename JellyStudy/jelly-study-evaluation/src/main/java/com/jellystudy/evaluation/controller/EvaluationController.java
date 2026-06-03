package com.jellystudy.evaluation.controller;

import com.jellystudy.evaluation.api.AnswerEvaluationResult;
import com.jellystudy.evaluation.api.QuestionEvaluationResult;
import com.jellystudy.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    @GetMapping("/question/{questionId}")
    public Map<String, Object> getQuestionEvaluation(@PathVariable String questionId) {
        QuestionEvaluationResult result = evaluationService.getQuestionEvaluation(questionId);
        return Map.of(
            "success", result.isSuccess(),
            "data", result
        );
    }

    @GetMapping("/answer/{answerId}")
    public Map<String, Object> getAnswerEvaluation(@PathVariable String answerId) {
        AnswerEvaluationResult result = evaluationService.getAnswerEvaluation(answerId);
        return Map.of(
            "success", result.isSuccess(),
            "data", result
        );
    }

    @PostMapping("/question/evaluate")
    public Map<String, Object> evaluateQuestion(@RequestBody Map<String, String> request) {
        String questionId = request.get("questionId");
        String questionTitle = request.get("questionTitle");
        String questionContent = request.get("questionContent");
        
        QuestionEvaluationResult result = evaluationService.evaluateQuestion(
            questionId, questionTitle, questionContent
        );
        
        return Map.of(
            "success", result.isSuccess(),
            "data", result
        );
    }

    @PostMapping("/answer/evaluate")
    public Map<String, Object> evaluateAnswer(@RequestBody Map<String, String> request) {
        String answerId = request.get("answerId");
        String questionId = request.get("questionId");
        String questionContent = request.get("questionContent");
        String answerContent = request.get("answerContent");
        
        AnswerEvaluationResult result = evaluationService.evaluateAnswer(
            answerId, questionId, questionContent, answerContent
        );
        
        return Map.of(
            "success", result.isSuccess(),
            "data", result
        );
    }
}

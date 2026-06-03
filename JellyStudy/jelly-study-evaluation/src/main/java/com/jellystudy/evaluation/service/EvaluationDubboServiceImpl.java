package com.jellystudy.evaluation.service;

import com.jellystudy.evaluation.api.AnswerEvaluationResult;
import com.jellystudy.evaluation.api.EvaluationDubboService;
import com.jellystudy.evaluation.api.QuestionEvaluationResult;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DubboService(version = "1.0.0", group = "evaluation", timeout = 30000)
public class EvaluationDubboServiceImpl implements EvaluationDubboService {
    
    @Autowired
    private EvaluationService evaluationService;
    
    @Override
    public QuestionEvaluationResult evaluateQuestion(String questionId, String questionTitle, String questionContent) {
        return evaluationService.evaluateQuestion(questionId, questionTitle, questionContent);
    }
    
    @Override
    public AnswerEvaluationResult evaluateAnswer(String answerId, String questionId, String questionContent, String answerContent) {
        return evaluationService.evaluateAnswer(answerId, questionId, questionContent, answerContent);
    }
    
    @Override
    public QuestionEvaluationResult getQuestionEvaluation(String questionId) {
        return evaluationService.getQuestionEvaluation(questionId);
    }
    
    @Override
    public AnswerEvaluationResult getAnswerEvaluation(String answerId) {
        return evaluationService.getAnswerEvaluation(answerId);
    }
    
    @Override
    public List<QuestionEvaluationResult> getQuestionEvaluations(List<String> questionIds) {
        return evaluationService.getQuestionEvaluations(questionIds);
    }
    
    @Override
    public List<AnswerEvaluationResult> getAnswerEvaluations(List<String> answerIds) {
        return evaluationService.getAnswerEvaluations(answerIds);
    }
}

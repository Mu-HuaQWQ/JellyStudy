package com.jellystudy.api;

import java.util.List;

public interface EvaluationDubboService {
    
    QuestionEvaluationResult evaluateQuestion(String questionId, String questionTitle, String questionContent);
    
    AnswerEvaluationResult evaluateAnswer(String answerId, String questionId, String questionContent, String answerContent);
    
    QuestionEvaluationResult getQuestionEvaluation(String questionId);
    
    AnswerEvaluationResult getAnswerEvaluation(String answerId);
    
    List<QuestionEvaluationResult> getQuestionEvaluations(List<String> questionIds);
    
    List<AnswerEvaluationResult> getAnswerEvaluations(List<String> answerIds);
}

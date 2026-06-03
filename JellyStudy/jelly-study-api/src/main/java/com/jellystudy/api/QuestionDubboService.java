package com.jellystudy.api;

import java.util.List;

public interface QuestionDubboService {
    
    QuestionDTO createQuestion(QuestionDTO questionDTO);
    
    QuestionDTO getQuestionById(String id);
    
    List<QuestionDTO> getAllQuestions();
    
    List<QuestionDTO> getQuestionsByAuthorId(String authorId);
    
    List<QuestionDTO> getQuestionsByKnowledgePointId(String knowledgePointId);
    
    List<QuestionDTO> searchQuestions(String keyword);
    
    QuestionDTO updateQuestion(String id, QuestionDTO questionDTO);
    
    void deleteQuestion(String id);
    
    void deleteQuestion(String id, String userId);
    
    Long count();
    
    List<QuestionDTO> getHotQuestions(int limit);
    
    List<QuestionDTO> getTopViewed(int limit);
    
    QuestionDTO likeQuestion(String questionId, String userId);
    
    QuestionDTO unlikeQuestion(String questionId, String userId);
}

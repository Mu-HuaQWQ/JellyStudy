package com.jellystudy.service;

import com.jellystudy.entity.Question;
import com.jellystudy.entity.QuestionRequest;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface QuestionService {
    
    Question create(QuestionRequest questionRequest, String authorId, String authorName);
    
    Optional<Question> findById(String id);
    
    List<Question> findAll();
    
    Page<Question> findAll(int page, int size);
    
    List<Question> findByAuthorId(String authorId);
    
    List<Question> findByKnowledgePointId(String knowledgePointId);
    
    List<Question> search(String keyword);
    
    Question update(String id, Question question);
    
    void delete(String id);
    
    void delete(String id, String userId);
    
    Long count();
    
    List<Question> findHotQuestions(int limit);
    
    List<Question> findTopViewed(int limit);
    
    List<Question> getRecommendQuestions(int limit);
    
    Question likeQuestion(String questionId, String userId);
    
    Question unlikeQuestion(String questionId, String userId);
    
    Question incrementViewCount(String questionId);
}

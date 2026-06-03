package com.jellystudy.service;

import com.jellystudy.entity.Answer;
import com.jellystudy.entity.AnswerRequest;

import java.util.List;
import java.util.Optional;

public interface AnswerService {
    
    Answer create(AnswerRequest answerRequest, String authorId, String authorName);
    
    Optional<Answer> findById(String id);
    
    List<Answer> findByQuestionId(String questionId);
    
    List<Answer> findByAuthorId(String authorId);
    
    Answer update(String id, Answer answer);
    
    void delete(String id);
    
    void delete(String id, String userId);
    
    Long count();
    
    Answer acceptAnswer(String answerId, String userId);
    
    Answer likeAnswer(String answerId, String userId);
    
    Answer unlikeAnswer(String answerId, String userId);
    
    List<Answer> findTopLikedAnswers(String questionId, int limit);
    
    List<Answer> findHotAnswers(int limit);
    
    Answer findAcceptedAnswer(String questionId);
}

package com.jellystudy.api;

import java.util.List;

public interface AnswerDubboService {
    
    AnswerDTO createAnswer(AnswerDTO answerDTO);
    
    AnswerDTO getAnswerById(String id);
    
    List<AnswerDTO> getAnswersByQuestionId(String questionId);
    
    List<AnswerDTO> getAnswersByAuthorId(String authorId);
    
    AnswerDTO updateAnswer(String id, AnswerDTO answerDTO);
    
    void deleteAnswer(String id);
    
    void deleteAnswer(String id, String userId);
    
    AnswerDTO acceptAnswer(String answerId, String userId);
    
    AnswerDTO likeAnswer(String answerId, String userId);
    
    AnswerDTO unlikeAnswer(String answerId, String userId);
    
    Long count();
}

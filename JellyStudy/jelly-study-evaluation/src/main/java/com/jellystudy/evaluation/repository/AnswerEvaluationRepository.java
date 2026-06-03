package com.jellystudy.evaluation.repository;

import com.jellystudy.evaluation.entity.AnswerEvaluation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerEvaluationRepository extends MongoRepository<AnswerEvaluation, String> {
    
    Optional<AnswerEvaluation> findFirstByAnswerIdOrderByCreateTimeDesc(String answerId);
    
    List<AnswerEvaluation> findByQuestionId(String questionId);
    
    List<AnswerEvaluation> findByAnswerIdIn(List<String> answerIds);
}

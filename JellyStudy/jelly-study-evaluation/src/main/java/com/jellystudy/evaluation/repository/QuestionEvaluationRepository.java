package com.jellystudy.evaluation.repository;

import com.jellystudy.evaluation.entity.QuestionEvaluation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionEvaluationRepository extends MongoRepository<QuestionEvaluation, String> {
    
    Optional<QuestionEvaluation> findByQuestionId(String questionId);
    
    List<QuestionEvaluation> findByDifficultyLevel(QuestionEvaluation.DifficultyLevel difficultyLevel);
    
    List<QuestionEvaluation> findByQuestionIdIn(List<String> questionIds);
}

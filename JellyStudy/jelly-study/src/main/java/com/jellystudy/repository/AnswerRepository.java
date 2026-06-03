package com.jellystudy.repository;

import com.jellystudy.entity.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends MongoRepository<Answer, String> {
    
    List<Answer> findByQuestionId(String questionId);
    
    @Query("{ 'questionId': ?0, 'isDeleted': false }")
    Page<Answer> findByQuestionIdNotDeleted(String questionId, Pageable pageable);
    
    @Query("{ 'questionId': ?0, 'isDeleted': false }")
    List<Answer> findAllByQuestionIdNotDeleted(String questionId);
    
    @Query("{ 'authorId': ?0 }")
    List<Answer> findByAuthorId(String authorId);
    
    @Query(value = "{ 'questionId': ?0, 'isDeleted': false, 'likeCount': { $gte: ?1 } }", sort = "{ 'likeCount': -1 }")
    List<Answer> findTopLikedAnswers(String questionId, Integer minLikes);
    
    @Query(value = "{ 'questionId': ?0, 'isDeleted': false, 'isAccepted': true }")
    Answer findAcceptedAnswer(String questionId);
    
    @Query(value = "{ 'isDeleted': false }", sort = "{ 'likeCount': -1 }")
    List<Answer> findTopLiked(Pageable pageable);
    
    @Query(value = "{ 'isDeleted': false, 'likeCount': { $gte: ?0 } }", sort = "{ 'likeCount': -1 }")
    List<Answer> findHotAnswers(Integer minLikes);
}

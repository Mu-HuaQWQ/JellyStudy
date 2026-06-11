package com.jellystudy.repository;

import com.jellystudy.entity.Question;
import com.jellystudy.entity.Question.QuestionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuestionRepository extends MongoRepository<Question, String> {
    
    List<Question> findByAuthorId(String authorId);
    
    List<Question> findByKnowledgePointId(String knowledgePointId);
    
    Page<Question> findByStatus(QuestionStatus status, Pageable pageable);
    
    @Query("{ 'title': { $regex: ?0, $options: 'i' } }")
    List<Question> searchByTitle(String keyword);
    
    @Query("{ 'tags': { $in: ?0 } }")
    List<Question> findByTagsContaining(List<String> tags);
    
    @Query("{ 'isDeleted': false }")
    Page<Question> findAllNotDeleted(Pageable pageable);
    
    @Query(value = "{ 'isDeleted': false }", count = true)
    Long countNotDeleted();
    
    @Query(value = "{ 'isDeleted': false, 'likeCount': { $gte: ?0 } }", sort = "{ 'likeCount': -1 }")
    List<Question> findHotQuestions(Integer minLikes);
    
    @Query(value = "{ 'knowledgePointId': ?0, 'isDeleted': false }", count = true)
    Long countByKnowledgePoint(String knowledgePointId);
    
    @Query(value = "{ 'isDeleted': false }", sort = "{ 'viewCount': -1 }")
    List<Question> findTopViewed(Pageable pageable);
    
    @Query(value = "{ 'isDeleted': false }", sort = "{ 'answerCount': -1, 'likeCount': -1 }")
    List<Question> findMostAnswered(Pageable pageable);

    @Query("{ 'isDeleted': false, '_id': { $ne: ?0 } }")
    List<Question> findAllNotDeletedExcluding(String excludeId);

    List<Question> findByAuthorIdAndCreateTimeAfter(String authorId, LocalDateTime after);

    long countByAuthorIdAndCreateTimeAfter(String authorId, LocalDateTime after);
}

package com.jellystudy.repository;

import com.jellystudy.entity.KnowledgePoint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgePointRepository extends MongoRepository<KnowledgePoint, String> {
    
    List<KnowledgePoint> findByCategory(String category);
    
    List<KnowledgePoint> findByTagsContaining(String tag);
    
    List<KnowledgePoint> findByIsPublished(Boolean isPublished);
    
    @Query("{ 'title': { $regex: ?0, $options: 'i' } }")
    List<KnowledgePoint> searchByTitle(String keyword);
    
    List<KnowledgePoint> findByParentId(String parentId);
    
    @Query(value = "{ 'path': { $regex: ?0 } }", fields = "{ 'title': 1, 'path': 1, 'category': 1 }")
    List<KnowledgePoint> findByPathStartingWith(String pathPrefix);
    
    List<KnowledgePoint> findTopByOrderByViewCountDesc(Pageable pageable);
}

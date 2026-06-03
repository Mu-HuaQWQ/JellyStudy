package com.jellystudy.service;

import com.jellystudy.entity.KnowledgePoint;

import java.util.List;
import java.util.Optional;

public interface KnowledgePointService {
    
    KnowledgePoint create(KnowledgePoint knowledgePoint);
    
    Optional<KnowledgePoint> findById(String id);
    
    List<KnowledgePoint> findAll();
    
    List<KnowledgePoint> findByCategory(String category);
    
    List<KnowledgePoint> findByTag(String tag);
    
    List<KnowledgePoint> search(String keyword);
    
    KnowledgePoint update(KnowledgePoint knowledgePoint);
    
    void delete(String id);
    
    Long count();
    
    List<KnowledgePoint> findByParentId(String parentId);
    
    List<KnowledgePoint> getRecommendKnowledgePoints(int limit);
}

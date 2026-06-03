package com.jellystudy.service;

import com.jellystudy.entity.KnowledgePoint;
import com.jellystudy.repository.KnowledgePointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KnowledgePointServiceImpl implements KnowledgePointService {

    @Autowired
    private KnowledgePointRepository knowledgePointRepository;

    @Override
    public KnowledgePoint create(KnowledgePoint knowledgePoint) {
        if (knowledgePoint.getViewCount() == null) {
            knowledgePoint.setViewCount(0);
        }
        if (knowledgePoint.getQuestionCount() == null) {
            knowledgePoint.setQuestionCount(0);
        }
        if (knowledgePoint.getIsPublished() == null) {
            knowledgePoint.setIsPublished(true);
        }
        return knowledgePointRepository.save(knowledgePoint);
    }

    @Override
    public Optional<KnowledgePoint> findById(String id) {
        return knowledgePointRepository.findById(id);
    }

    @Override
    public List<KnowledgePoint> findAll() {
        return knowledgePointRepository.findAll();
    }

    @Override
    public List<KnowledgePoint> findByCategory(String category) {
        return knowledgePointRepository.findByCategory(category);
    }

    @Override
    public List<KnowledgePoint> findByTag(String tag) {
        return knowledgePointRepository.findByTagsContaining(tag);
    }

    @Override
    public List<KnowledgePoint> search(String keyword) {
        return knowledgePointRepository.searchByTitle(keyword);
    }

    @Override
    public KnowledgePoint update(KnowledgePoint knowledgePoint) {
        return knowledgePointRepository.save(knowledgePoint);
    }

    @Override
    public void delete(String id) {
        knowledgePointRepository.deleteById(id);
    }

    @Override
    public Long count() {
        return knowledgePointRepository.count();
    }

    @Override
    public List<KnowledgePoint> findByParentId(String parentId) {
        return knowledgePointRepository.findByParentId(parentId);
    }

    @Override
    public List<KnowledgePoint> getRecommendKnowledgePoints(int limit) {
        return knowledgePointRepository.findTopByOrderByViewCountDesc(
                PageRequest.of(0, limit)
        );
    }
}

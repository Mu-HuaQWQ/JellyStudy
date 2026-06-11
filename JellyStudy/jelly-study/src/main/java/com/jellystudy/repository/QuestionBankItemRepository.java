package com.jellystudy.repository;

import com.jellystudy.entity.QuestionBankItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionBankItemRepository extends MongoRepository<QuestionBankItem, String> {

    List<QuestionBankItem> findByKnowledgePointId(String knowledgePointId);

    long countByKnowledgePointId(String knowledgePointId);

    void deleteByKnowledgePointId(String knowledgePointId);
}

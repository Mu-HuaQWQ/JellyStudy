package com.jellystudy.repository;

import com.jellystudy.entity.BrowseHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrowseHistoryRepository extends MongoRepository<BrowseHistory, String> {

    Optional<BrowseHistory> findByUserIdAndQuestionId(String userId, String questionId);

    List<BrowseHistory> findByUserIdOrderByViewTimeDesc(String userId);
}

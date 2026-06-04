package com.jellystudy.repository;

import com.jellystudy.entity.Favorite;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends MongoRepository<Favorite, String> {

    Optional<Favorite> findByUserIdAndQuestionId(String userId, String questionId);

    boolean existsByUserIdAndQuestionId(String userId, String questionId);

    List<Favorite> findByUserIdOrderByCreateTimeDesc(String userId);

    void deleteByUserIdAndQuestionId(String userId, String questionId);
}

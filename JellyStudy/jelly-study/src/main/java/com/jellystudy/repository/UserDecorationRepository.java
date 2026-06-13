package com.jellystudy.repository;

import com.jellystudy.entity.UserDecoration;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDecorationRepository extends MongoRepository<UserDecoration, String> {
    List<UserDecoration> findByUserId(String userId);
    Optional<UserDecoration> findByUserIdAndItemId(String userId, String itemId);
    List<UserDecoration> findByUserIdAndEquippedTrue(String userId);
    List<UserDecoration> findByUserIdAndItemType(String userId, String itemType);
    List<UserDecoration> findByUserIdAndEquippedTrueAndItemType(String userId, String itemType);
}

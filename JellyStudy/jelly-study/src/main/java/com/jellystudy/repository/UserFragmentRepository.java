package com.jellystudy.repository;

import com.jellystudy.entity.UserFragment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserFragmentRepository extends MongoRepository<UserFragment, String> {
    List<UserFragment> findByUserId(String userId);
    Optional<UserFragment> findByUserIdAndItemId(String userId, String itemId);
    void deleteByItemIdNotIn(Set<String> itemIds);
}

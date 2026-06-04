package com.jellystudy.repository;

import com.jellystudy.entity.Follow;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends MongoRepository<Follow, String> {

    Optional<Follow> findByFollowerIdAndFollowingId(String followerId, String followingId);

    boolean existsByFollowerIdAndFollowingId(String followerId, String followingId);

    // 我关注的人
    List<Follow> findByFollowerIdOrderByCreateTimeDesc(String followerId);

    // 关注我的人（粉丝）
    List<Follow> findByFollowingIdOrderByCreateTimeDesc(String followingId);

    long countByFollowerId(String followerId);

    long countByFollowingId(String followingId);
}

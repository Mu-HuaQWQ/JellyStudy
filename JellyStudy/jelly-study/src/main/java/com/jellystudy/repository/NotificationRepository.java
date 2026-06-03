package com.jellystudy.repository;

import com.jellystudy.entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    List<Notification> findByUserIdOrderByCreateTimeDesc(String userId);

    List<Notification> findByUserIdAndReadFalseOrderByCreateTimeDesc(String userId);

    long countByUserIdAndReadFalse(String userId);

    void deleteByUserIdAndReadTrue(String userId);
}
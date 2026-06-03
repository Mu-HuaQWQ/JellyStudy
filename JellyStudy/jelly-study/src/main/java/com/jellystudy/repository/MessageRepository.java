package com.jellystudy.repository;

import com.jellystudy.entity.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    List<Message> findByReceiverIdOrderByCreateTimeDesc(String receiverId);

    List<Message> findBySenderIdAndReceiverIdOrderByCreateTimeDesc(String senderId, String receiverId);

    List<Message> findBySenderIdOrReceiverIdOrderByCreateTimeDesc(String userId1, String userId2);

    long countByReceiverIdAndStatus(String receiverId, Message.MessageStatus status);
}
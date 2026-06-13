package com.jellystudy.service;

import com.jellystudy.config.NacosConfig;
import com.jellystudy.entity.Message;
import com.jellystudy.entity.User;
import com.jellystudy.repository.MessageRepository;
import com.jellystudy.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private NacosConfig nacosConfig;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Message sendMessage(Message message) {
        if (message.getContent() == null || message.getContent().length() > nacosConfig.getMessageMaxLength()) {
            throw new RuntimeException("消息内容不能为空且不能超过" + nacosConfig.getMessageMaxLength() + "个字符");
        }

        message.setCreateTime(LocalDateTime.now());
        message.setStatus(Message.MessageStatus.UNREAD);
        Message saved = messageRepository.save(message);

        try {
            Map<String, Object> mqMessage = new HashMap<>();
            mqMessage.put("type", "NEW_MESSAGE");
            mqMessage.put("messageId", saved.getId());
            mqMessage.put("senderId", saved.getSenderId());
            mqMessage.put("senderName", saved.getSenderName());
            mqMessage.put("receiverId", saved.getReceiverId());
            mqMessage.put("content", saved.getContent());

            rabbitTemplate.convertAndSend(
                com.jellystudy.config.RabbitMQConfig.MESSAGE_EXCHANGE,
                "message.new",
                mqMessage
            );
        } catch (Exception e) {
            logger.warn("Failed to send message to RabbitMQ: {}", e.getMessage());
        }

        logger.info("Sent message from {} to {}", saved.getSenderId(), saved.getReceiverId());
        return saved;
    }

    @Override
    public List<Message> getConversation(String user1Id, String user2Id) {
        // 双向查询：user1→user2 和 user2→user1
        List<Message> sent = messageRepository.findBySenderIdAndReceiverIdOrderByCreateTimeDesc(user1Id, user2Id);
        List<Message> received = messageRepository.findBySenderIdAndReceiverIdOrderByCreateTimeDesc(user2Id, user1Id);
        sent.addAll(received);
        // 按时间降序排列（最新在前）
        sent.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
        return sent;
    }

    @Override
    public List<Message> getUserMessages(String userId, int page, int size) {
        // 查询该用户收发过的所有消息，用于构建联系人列表
        List<Message> all = messageRepository.findBySenderIdOrReceiverIdOrderByCreateTimeDesc(userId, userId);
        return all.stream()
                .skip((long) page * size)
                .limit(size)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public long getUnreadCount(String userId) {
        return messageRepository.countByReceiverIdAndStatus(userId, Message.MessageStatus.UNREAD);
    }

    @Override
    public void markMessageAsRead(String messageId, String userId) {
        Message message = messageRepository.findById(messageId).orElse(null);
        if (message != null && message.getReceiverId().equals(userId)) {
            message.setStatus(Message.MessageStatus.READ);
            message.setReadTime(LocalDateTime.now());
            messageRepository.save(message);
            logger.info("Marked message {} as read for user {}", messageId, userId);
        }
    }

    @Override
    public void markAllMessagesAsRead(String senderId, String receiverId) {
        List<Message> messages = messageRepository.findBySenderIdAndReceiverIdOrderByCreateTimeDesc(senderId, receiverId)
                .stream()
                .filter(m -> m.getStatus() == Message.MessageStatus.UNREAD)
                .collect(java.util.stream.Collectors.toList());

        for (Message message : messages) {
            message.setStatus(Message.MessageStatus.READ);
            message.setReadTime(LocalDateTime.now());
            messageRepository.save(message);
        }
        logger.info("Marked all messages from {} as read for user {}", senderId, receiverId);
    }

    @Override
    public Map<String, Object> getMessageStats(String userId) {
        Map<String, Object> stats = new HashMap<>();
        long totalReceived = messageRepository.findByReceiverIdOrderByCreateTimeDesc(userId).size();
        long totalSent = messageRepository.findBySenderIdOrReceiverIdOrderByCreateTimeDesc(userId, userId)
                .stream()
                .filter(m -> m.getSenderId().equals(userId))
                .count();
        
        stats.put("totalReceived", totalReceived);
        stats.put("totalSent", totalSent);
        stats.put("unread", getUnreadCount(userId));
        stats.put("timestamp", System.currentTimeMillis());
        return stats;
    }

    @Override
    public List<Map<String, Object>> getContacts(String userId) {
        List<Message> all = messageRepository.findBySenderIdOrReceiverIdOrderByCreateTimeDesc(userId, userId);

        // 用 LinkedHashMap 保持插入顺序
        Map<String, Map<String, Object>> contactMap = new LinkedHashMap<>();

        for (Message msg : all) {
            String otherId = msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId();
            if (otherId == null || otherId.equals(userId)) continue;

            Map<String, Object> c = contactMap.get(otherId);
            if (c == null) {
                // 解析用户信息
                String displayName = otherId;
                String avatar = null;
                Optional<User> userOpt = userRepository.findById(otherId);
                if (userOpt.isPresent()) {
                    User u = userOpt.get();
                    displayName = (u.getNickname() != null && !u.getNickname().isEmpty())
                        ? u.getNickname() : (u.getUsername() != null ? u.getUsername() : otherId);
                    avatar = u.getAvatar();
                }

                c = new LinkedHashMap<>();
                c.put("userId", otherId);
                c.put("displayName", displayName);
                c.put("avatar", avatar);
                c.put("lastMessage", msg.getContent());
                c.put("lastTime", msg.getCreateTime().toString());
                c.put("unread", 0);
                contactMap.put(otherId, c);
            }

            // 更新最后消息
            if (msg.getCreateTime() != null) {
                Map<String, Object> existing = contactMap.get(otherId);
                String existingTime = (String) existing.get("lastTime");
                if (existingTime == null || msg.getCreateTime().toString().compareTo(existingTime) > 0) {
                    existing.put("lastMessage", msg.getContent());
                    existing.put("lastTime", msg.getCreateTime().toString());
                }
            }

            // 未读计数
            if (msg.getReceiverId().equals(userId) && msg.getStatus() == Message.MessageStatus.UNREAD) {
                Map<String, Object> existing = contactMap.get(otherId);
                existing.put("unread", ((Number) existing.get("unread")).intValue() + 1);
            }
        }

        return new ArrayList<>(contactMap.values());
    }
}
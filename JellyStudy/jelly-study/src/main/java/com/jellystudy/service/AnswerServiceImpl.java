package com.jellystudy.service;

import com.jellystudy.client.EvaluationClient;
import com.jellystudy.entity.Answer;
import com.jellystudy.entity.AnswerRequest;
import com.jellystudy.entity.Question;
import com.jellystudy.entity.Question.QuestionStatus;
import com.jellystudy.entity.User;
import com.jellystudy.repository.AnswerRepository;
import com.jellystudy.repository.QuestionRepository;
import com.jellystudy.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class AnswerServiceImpl implements AnswerService {
    
    private static final Logger logger = LoggerFactory.getLogger(AnswerServiceImpl.class);

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private EvaluationClient evaluationClient;

    @Autowired
    private NotificationIntegrationService notificationIntegrationService;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Answer create(AnswerRequest answerRequest, String authorId, String authorName) {
        Optional<Question> questionOpt = questionRepository.findById(answerRequest.getQuestionId());
        
        Answer answer = Answer.builder()
                .questionId(answerRequest.getQuestionId())
                .content(answerRequest.getContent())
                .authorId(authorId)
                .authorName(authorName)
                .isAccepted(false)
                .likedByUsers(new HashSet<>())
                .likeCount(0)
                .isDeleted(false)
                .comments(new ArrayList<>())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .commentCount(0)
                .build();
        
        Answer savedAnswer = answerRepository.save(answer);
        
        final String questionTitle;
        final String questionContent;
        
        if (questionOpt.isPresent()) {
            Question question = questionOpt.get();
            questionTitle = question.getTitle();
            questionContent = question.getContent();
            question.setAnswerCount(question.getAnswerCount() + 1);
            question.setStatus(QuestionStatus.ANSWERED);
            question.setLastAnswerTime(LocalDateTime.now());
            question.setUpdateTime(LocalDateTime.now());
            questionRepository.save(question);
        } else {
            questionTitle = "";
            questionContent = "";
        }
        
        logger.info("Answer created successfully: {}", savedAnswer.getId());
        
        evaluateAnswerAsync(savedAnswer, questionTitle, questionContent);
        
        if (questionOpt.isPresent()) {
            Question question = questionOpt.get();
            if (!authorId.equals(question.getAuthorId())) {
                notificationIntegrationService.notifyQuestionAnswered(
                    question.getAuthorId(),
                    answerRequest.getQuestionId(),
                    authorName
                );
            }
        }
        
        return savedAnswer;
    }

    private void evaluateAnswerAsync(Answer answer, String questionTitle, String questionContent) {
        try {
            CompletableFuture<Map<String, Object>> future = evaluationClient.evaluateAnswerAsync(
                answer.getId(),
                questionTitle,
                questionContent,
                answer.getContent()
            );
            
            future.thenAccept(result -> {
                if (result != null && Boolean.TRUE.equals(result.get("success"))) {
                    logger.info("Answer evaluation completed for answer: {}", answer.getId());
                } else {
                    logger.warn("Answer evaluation failed for answer: {}", answer.getId());
                }
            }).exceptionally(ex -> {
                logger.error("Error during answer evaluation", ex);
                return null;
            });
        } catch (Exception e) {
            logger.warn("Evaluation service not available, skipping answer evaluation");
        }
    }

    @Override
    public Optional<Answer> findById(String id) {
        return answerRepository.findById(id);
    }

    @Override
    public List<Answer> findByQuestionId(String questionId) {
        return answerRepository.findAllByQuestionIdNotDeleted(questionId);
    }

    @Override
    public List<Answer> findByAuthorId(String authorId) {
        return answerRepository.findByAuthorId(authorId);
    }

    @Override
    public Answer update(String id, Answer answer) {
        Optional<Answer> existingOpt = answerRepository.findById(id);
        if (existingOpt.isPresent()) {
            Answer existing = existingOpt.get();
            if (answer.getContent() != null) {
                existing.setContent(answer.getContent());
            }
            existing.setUpdateTime(LocalDateTime.now());
            return answerRepository.save(existing);
        }
        throw new RuntimeException("Answer not found with id: " + id);
    }

    @Override
    public void delete(String id) {
        Optional<Answer> answerOpt = answerRepository.findById(id);
        answerOpt.ifPresent(answer -> {
            answer.setIsDeleted(true);
            answer.setDeletedReason("User deleted");
            answer.setUpdateTime(LocalDateTime.now());
            answerRepository.save(answer);
            
            questionRepository.findById(answer.getQuestionId()).ifPresent(question -> {
                question.setAnswerCount(Math.max(0, question.getAnswerCount() - 1));
                questionRepository.save(question);
            });
        });
    }

    @Override
    public Long count() {
        return answerRepository.count();
    }

    @Override
    public void delete(String id, String userId) {
        Optional<Answer> answerOpt = answerRepository.findById(id);
        if (answerOpt.isEmpty()) {
            throw new RuntimeException("Answer not found");
        }
        Answer answer = answerOpt.get();
        if (!userId.equals(answer.getAuthorId())) {
            throw new RuntimeException("You can only delete your own answer");
        }
        delete(id);
    }

    @Override
    public Answer acceptAnswer(String answerId, String userId) {
        Optional<Answer> answerOpt = answerRepository.findById(answerId);
        if (answerOpt.isPresent()) {
            Answer answer = answerOpt.get();
            
            Optional<Question> questionOpt = questionRepository.findById(answer.getQuestionId());
            if (questionOpt.isEmpty()) {
                throw new RuntimeException("Question not found");
            }
            Question question = questionOpt.get();
            
            if (!userId.equals(question.getAuthorId())) {
                throw new RuntimeException("只有问题作者才能采纳回答");
            }
            
            List<Answer> allAnswers = answerRepository.findByQuestionId(answer.getQuestionId());
            for (Answer a : allAnswers) {
                if (a.getIsAccepted()) {
                    a.setIsAccepted(false);
                    answerRepository.save(a);
                }
            }
            
            answer.setIsAccepted(true);
            answer.setUpdateTime(LocalDateTime.now());
            Answer savedAnswer = answerRepository.save(answer);
            
            question.setStatus(QuestionStatus.CLOSED);
            questionRepository.save(question);
            
            return savedAnswer;
        }
        throw new RuntimeException("Answer not found with id: " + answerId);
    }

    @Override
    public Answer likeAnswer(String answerId, String userId) {
        Optional<Answer> answerOpt = answerRepository.findById(answerId);
        if (answerOpt.isPresent()) {
            Answer answer = answerOpt.get();
            if (answer.getLikedByUsers() == null) {
                answer.setLikedByUsers(new HashSet<>());
            }
            if (!answer.getLikedByUsers().contains(userId)) {
                answer.getLikedByUsers().add(userId);
                answer.setLikeCount(answer.getLikedByUsers().size());
                answer.setUpdateTime(LocalDateTime.now());
                Answer saved = answerRepository.save(answer);

                if (!userId.equals(answer.getAuthorId())) {
                    String likerName = userId;
                    Optional<User> userOpt = userRepository.findById(userId);
                    if (userOpt.isPresent() && userOpt.get().getNickname() != null) {
                        likerName = userOpt.get().getNickname();
                    }
                    notificationIntegrationService.notifyAnswerLiked(
                        answer.getAuthorId(),
                        answerId,
                        likerName
                    );
                }

                return saved;
            }
            return answer;
        }
        throw new RuntimeException("Answer not found with id: " + answerId);
    }

    @Override
    public Answer unlikeAnswer(String answerId, String userId) {
        Optional<Answer> answerOpt = answerRepository.findById(answerId);
        if (answerOpt.isPresent()) {
            Answer answer = answerOpt.get();
            if (answer.getLikedByUsers() != null && answer.getLikedByUsers().contains(userId)) {
                answer.getLikedByUsers().remove(userId);
                answer.setLikeCount(answer.getLikedByUsers().size());
                answer.setUpdateTime(LocalDateTime.now());
                return answerRepository.save(answer);
            }
            return answer;
        }
        throw new RuntimeException("Answer not found with id: " + answerId);
    }

    @Override
    public List<Answer> findTopLikedAnswers(String questionId, int limit) {
        return answerRepository.findTopLikedAnswers(questionId, 0);
    }

    @Override
    public List<Answer> findHotAnswers(int limit) {
        return answerRepository.findHotAnswers(0);
    }

    @Override
    public Answer findAcceptedAnswer(String questionId) {
        return answerRepository.findAcceptedAnswer(questionId);
    }
}

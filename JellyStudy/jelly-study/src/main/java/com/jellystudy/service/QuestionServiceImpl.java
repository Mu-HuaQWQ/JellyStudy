package com.jellystudy.service;

import com.jellystudy.client.EvaluationClient;
import com.jellystudy.entity.KnowledgePoint;
import com.jellystudy.entity.Question;
import com.jellystudy.entity.Question.QuestionStatus;
import com.jellystudy.entity.QuestionRequest;
import com.jellystudy.repository.KnowledgePointRepository;
import com.jellystudy.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class QuestionServiceImpl implements QuestionService {
    
    private static final Logger logger = LoggerFactory.getLogger(QuestionServiceImpl.class);

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private KnowledgePointRepository knowledgePointRepository;

    @Autowired
    private EvaluationClient evaluationClient;
    
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RedisService redisService;

    @Override
    public Question create(QuestionRequest questionRequest, String authorId, String authorName) {
        String knowledgePointId = questionRequest.getKnowledgePointId();
        String knowledgePointTitle = "";
        
        if (knowledgePointId != null && !knowledgePointId.isEmpty()) {
            Optional<KnowledgePoint> kpOpt = knowledgePointRepository.findById(knowledgePointId);
            knowledgePointTitle = kpOpt.map(KnowledgePoint::getTitle).orElse("");
        }
        
        Question question = Question.builder()
                .title(questionRequest.getTitle())
                .content(questionRequest.getContent())
                .authorId(authorId)
                .authorName(authorName)
                .knowledgePointId(knowledgePointId)
                .knowledgePointTitle(knowledgePointTitle)
                .tags(questionRequest.getTags() != null ? questionRequest.getTags() : new ArrayList<>())
                .status(QuestionStatus.PENDING)
                .viewCount(0)
                .answerCount(0)
                .likedByUsers(new HashSet<>())
                .likeCount(0)
                .isDeleted(false)
                .answers(new ArrayList<>())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        Question savedQuestion = questionRepository.save(question);
        
        if (knowledgePointId != null && !knowledgePointId.isEmpty()) {
            knowledgePointRepository.findById(knowledgePointId).ifPresent(kp -> {
                kp.setQuestionCount(kp.getQuestionCount() + 1);
                knowledgePointRepository.save(kp);
            });
        }
        
        logger.info("Question created successfully: {}", savedQuestion.getId());
        
        evaluateAndLinkKnowledgePoint(savedQuestion);
        
        return savedQuestion;
    }

    private void evaluateAndLinkKnowledgePoint(Question question) {
        try {
            CompletableFuture<Map<String, Object>> future = evaluationClient.evaluateQuestionAsync(
                question.getId(),
                question.getTitle(),
                question.getContent()
            );
            
            future.thenAccept(result -> {
                if (result != null && Boolean.TRUE.equals(result.get("success"))) {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> data = (Map<String, Object>) result.get("data");
                        if (data != null) {
                            @SuppressWarnings("unchecked")
                            List<String> knowledgePoints = (List<String>) data.get("extractedKnowledgePoints");
                            String difficulty = (String) data.get("difficultyLevel");
                            
                            if (knowledgePoints != null && !knowledgePoints.isEmpty()) {
                                String mainKnowledgePoint = knowledgePoints.get(0);
                                KnowledgePoint kp = findOrCreateKnowledgePoint(mainKnowledgePoint, difficulty);
                                
                                question.setKnowledgePointId(kp.getId());
                                question.setKnowledgePointTitle(kp.getTitle());
                                question.setUpdateTime(LocalDateTime.now());
                                questionRepository.save(question);
                                
                                kp.setQuestionCount(kp.getQuestionCount() + 1);
                                knowledgePointRepository.save(kp);
                                
                                logger.info("Question linked to knowledge point: {}", kp.getTitle());
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Error processing evaluation result", e);
                    }
                    logger.info("Question evaluation completed for question: {}", question.getId());
                } else {
                    logger.warn("Question evaluation failed for question: {}", question.getId());
                }
            }).exceptionally(ex -> {
                logger.error("Error during question evaluation", ex);
                return null;
            });
        } catch (Exception e) {
            logger.warn("Evaluation service not available, skipping question evaluation");
        }
    }
    
    private KnowledgePoint findOrCreateKnowledgePoint(String knowledgePointTitle, String difficulty) {
        Query query = new Query(Criteria.where("title").is(knowledgePointTitle));
        KnowledgePoint existing = mongoTemplate.findOne(query, KnowledgePoint.class);
        
        if (existing != null) {
            return existing;
        }
        
        KnowledgePoint newKp = KnowledgePoint.builder()
                .title(knowledgePointTitle)
                .content("知识点：" + knowledgePointTitle)
                .difficulty(difficulty != null ? difficulty : "MEDIUM")
                .authorId("system")
                .authorName("AI自动创建")
                .tags(new ArrayList<>())
                .viewCount(0)
                .questionCount(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        return knowledgePointRepository.save(newKp);
    }

    @Override
    public Optional<Question> findById(String id) {
        return questionRepository.findById(id);
    }

    @Override
    public List<Question> findAll() {
        return questionRepository.findAll();
    }

    @Override
    public Page<Question> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return questionRepository.findAllNotDeleted(pageable);
    }

    @Override
    public List<Question> findByAuthorId(String authorId) {
        return questionRepository.findByAuthorId(authorId);
    }

    @Override
    public List<Question> findByKnowledgePointId(String knowledgePointId) {
        return questionRepository.findByKnowledgePointId(knowledgePointId);
    }

    @Override
    public List<Question> search(String keyword) {
        return questionRepository.searchByTitle(keyword);
    }

    @Override
    public Question update(String id, Question question) {
        Optional<Question> existingOpt = questionRepository.findById(id);
        if (existingOpt.isPresent()) {
            Question existing = existingOpt.get();
            if (question.getTitle() != null) {
                existing.setTitle(question.getTitle());
            }
            if (question.getContent() != null) {
                existing.setContent(question.getContent());
            }
            if (question.getTags() != null) {
                existing.setTags(question.getTags());
            }
            if (question.getStatus() != null) {
                existing.setStatus(question.getStatus());
            }
            existing.setUpdateTime(LocalDateTime.now());
            return questionRepository.save(existing);
        }
        throw new RuntimeException("Question not found with id: " + id);
    }

    @Override
    public void delete(String id) {
        Optional<Question> questionOpt = questionRepository.findById(id);
        questionOpt.ifPresent(question -> {
            question.setIsDeleted(true);
            question.setDeletedReason("User deleted");
            question.setStatus(QuestionStatus.DELETED);
            question.setUpdateTime(LocalDateTime.now());
            questionRepository.save(question);
        });
    }

    @Override
    public Long count() {
        return questionRepository.countNotDeleted();
    }

    @Override
    public void delete(String id, String userId) {
        Optional<Question> questionOpt = questionRepository.findById(id);
        if (questionOpt.isEmpty()) {
            throw new RuntimeException("Question not found");
        }
        Question question = questionOpt.get();
        if (!userId.equals(question.getAuthorId())) {
            throw new RuntimeException("You can only delete your own question");
        }
        delete(id);
    }

    @Override
    public List<Question> findHotQuestions(int limit) {
        return questionRepository.findHotQuestions(0);
    }

    @Override
    public List<Question> findTopViewed(int limit) {
        return questionRepository.findTopViewed(PageRequest.of(0, limit));
    }

    @Override
    public List<Question> getRecommendQuestions(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "likeCount", "answerCount", "viewCount"));
        return questionRepository.findMostAnswered(pageable);
    }

    @Override
    public Question likeQuestion(String questionId, String userId) {
        Optional<Question> questionOpt = questionRepository.findById(questionId);
        if (questionOpt.isPresent()) {
            Question question = questionOpt.get();
            if (question.getLikedByUsers() == null) {
                question.setLikedByUsers(new HashSet<>());
            }
            if (!question.getLikedByUsers().contains(userId)) {
                question.getLikedByUsers().add(userId);
                question.setLikeCount(question.getLikedByUsers().size());
                question.setUpdateTime(LocalDateTime.now());
                Question saved = questionRepository.save(question);

                redisService.incrementQuestionPopularity(questionId);
                redisService.recordUserActivity(userId, "LIKE");

                return saved;
            }
            return question;
        }
        throw new RuntimeException("Question not found with id: " + questionId);
    }

    @Override
    public Question unlikeQuestion(String questionId, String userId) {
        Optional<Question> questionOpt = questionRepository.findById(questionId);
        if (questionOpt.isPresent()) {
            Question question = questionOpt.get();
            if (question.getLikedByUsers() != null && question.getLikedByUsers().contains(userId)) {
                question.getLikedByUsers().remove(userId);
                question.setLikeCount(question.getLikedByUsers().size());
                question.setUpdateTime(LocalDateTime.now());
                return questionRepository.save(question);
            }
            return question;
        }
        throw new RuntimeException("Question not found with id: " + questionId);
    }

    @Override
    public List<Map<String, Object>> findSimilarQuestions(String questionId, int limit) {
        Optional<Question> selfOpt = questionRepository.findById(questionId);
        if (selfOpt.isEmpty()) {
            return Collections.emptyList();
        }
        Question self = selfOpt.get();

        List<Question> candidates = questionRepository.findAllNotDeletedExcluding(questionId);

        List<Map<String, Object>> scored = new ArrayList<>();
        for (Question q : candidates) {
            double score = calculateSimilarity(self, q);
            if (score > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", q.getId());
                item.put("title", q.getTitle());
                item.put("knowledgePointTitle", q.getKnowledgePointTitle() != null ? q.getKnowledgePointTitle() : "");
                item.put("tags", q.getTags() != null ? q.getTags() : Collections.emptyList());
                item.put("similarity", Math.round(score * 100.0) / 100.0);
                scored.add(item);
            }
        }

        scored.sort((a, b) -> Double.compare(
            (Double) b.get("similarity"), (Double) a.get("similarity")));

        return scored.subList(0, Math.min(limit, scored.size()));
    }

    private double calculateSimilarity(Question a, Question b) {
        double knowledgePointScore = 0.0;
        String kpA = a.getKnowledgePointId();
        String kpB = b.getKnowledgePointId();
        if (kpA != null && !kpA.isEmpty() && kpB != null && !kpB.isEmpty() && kpA.equals(kpB)) {
            knowledgePointScore = 0.6;
        }

        double tagScore = 0.0;
        List<String> tagsA = a.getTags() != null ? a.getTags() : Collections.emptyList();
        List<String> tagsB = b.getTags() != null ? b.getTags() : Collections.emptyList();

        if (!tagsA.isEmpty() && !tagsB.isEmpty()) {
            long intersection = tagsA.stream().filter(tagsB::contains).count();
            int maxSize = Math.max(tagsA.size(), tagsB.size());
            double jaccard = (double) intersection / maxSize;
            tagScore = jaccard * 0.4;
        }

        return knowledgePointScore + tagScore;
    }

    @Override
    public Question incrementViewCount(String questionId) {
        Optional<Question> questionOpt = questionRepository.findById(questionId);
        if (questionOpt.isPresent()) {
            Question question = questionOpt.get();
            question.setViewCount(question.getViewCount() + 1);
            question.setUpdateTime(LocalDateTime.now());
            Question saved = questionRepository.save(question);

            redisService.incrementQuestionPopularity(questionId);
            if (question.getViewCount() > 10) {
                redisService.cacheHotViewedQuestion(saved);
            }

            return saved;
        }
        throw new RuntimeException("Question not found with id: " + questionId);
    }
}

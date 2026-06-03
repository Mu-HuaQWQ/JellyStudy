package com.jellystudy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jellystudy.entity.Question;
import com.jellystudy.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RedisService {

    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private QuestionRepository questionRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String POPULAR_RANKING_KEY = "question:popular:ranking";
    private static final String HOT_VIEW_CACHE_KEY = "question:hot:view:";
    private static final String USER_ACTIVITY_KEY = "user:activity:";
    private static final String ONLINE_USERS_KEY = "users:online";

    public void incrementQuestionPopularity(String questionId) {
        try {
            Optional<Question> questionOpt = questionRepository.findById(questionId);
            if (questionOpt.isPresent()) {
                Question question = questionOpt.get();
                double score = calculatePopularityScore(question);
                redisTemplate.opsForZSet().add(POPULAR_RANKING_KEY, questionId, score);
                logger.debug("Updated popularity score for question {}: {}", questionId, score);
            }
        } catch (Exception e) {
            logger.error("Error updating question popularity: {}", e.getMessage());
        }
    }

    public List<Map<String, Object>> getPopularQuestionsRanking(int limit) {
        try {
            Set<ZSetOperations.TypedTuple<Object>> ranking =
                    redisTemplate.opsForZSet().reverseRangeWithScores(POPULAR_RANKING_KEY, 0, limit - 1);

            if (ranking == null || ranking.isEmpty()) {
                logger.info("No popular questions in cache, syncing from database");
                syncPopularQuestionsFromDB();
                ranking = redisTemplate.opsForZSet().reverseRangeWithScores(POPULAR_RANKING_KEY, 0, limit - 1);
            }

            List<Map<String, Object>> result = new ArrayList<>();
            if (ranking != null) {
                int rank = 1;
                for (ZSetOperations.TypedTuple<Object> tuple : ranking) {
                    String questionId = tuple.getValue().toString();
                    Map<String, Object> item = new HashMap<>();
                    item.put("rank", rank++);
                    item.put("questionId", questionId);
                    item.put("score", tuple.getScore());

                    Optional<Question> questionOpt = questionRepository.findById(questionId);
                    if (questionOpt.isPresent()) {
                        Question q = questionOpt.get();
                        item.put("title", q.getTitle());
                        item.put("content", q.getContent() != null && q.getContent().length() > 100 
                            ? q.getContent().substring(0, 100) + "..." : q.getContent());
                        item.put("authorName", q.getAuthorName());
                        item.put("viewCount", q.getViewCount() != null ? q.getViewCount() : 0);
                        item.put("likeCount", q.getLikeCount() != null ? q.getLikeCount() : 0);
                        item.put("answerCount", q.getAnswerCount() != null ? q.getAnswerCount() : 0);
                    }
                    result.add(item);
                }
            }
            return result;
        } catch (Exception e) {
            logger.error("Error getting popular questions ranking: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public double calculatePopularityScore(Question question) {
        int likeCount = question.getLikeCount() != null ? question.getLikeCount() : 0;
        int answerCount = question.getAnswerCount() != null ? question.getAnswerCount() : 0;
        int viewCount = question.getViewCount() != null ? question.getViewCount() : 0;

        return (likeCount * 3.0) + (answerCount * 2.0) + (viewCount * 0.1);
    }

    @Scheduled(fixedRate = 3600000)
    public void syncPopularQuestionsFromDB() {
        try {
            logger.info("Syncing popular questions from database to Redis");
            redisTemplate.delete(POPULAR_RANKING_KEY);

            List<Question> questions = questionRepository.findMostAnswered(PageRequest.of(0, 100));
            for (Question question : questions) {
                double score = calculatePopularityScore(question);
                redisTemplate.opsForZSet().add(POPULAR_RANKING_KEY, question.getId(), score);
            }

            redisTemplate.expire(POPULAR_RANKING_KEY, 24, TimeUnit.HOURS);
            logger.info("Synced {} questions to popular ranking", questions.size());
        } catch (Exception e) {
            logger.error("Error syncing popular questions: {}", e.getMessage());
        }
    }

    public void cacheHotViewedQuestion(Question question) {
        try {
            String key = HOT_VIEW_CACHE_KEY + question.getId();
            String json = objectMapper.writeValueAsString(question);

            redisTemplate.opsForValue().set(key, json, Duration.ofMinutes(30));
            redisTemplate.opsForZSet().add("question:hot:viewed:ranking", question.getId(), question.getViewCount());

            logger.debug("Cached hot viewed question: {}", question.getId());
        } catch (JsonProcessingException e) {
            logger.error("Error caching hot viewed question: {}", e.getMessage());
        }
    }

    public Question getHotViewedQuestionFromCache(String questionId) {
        try {
            String key = HOT_VIEW_CACHE_KEY + questionId;
            Object cached = redisTemplate.opsForValue().get(key);

            if (cached != null) {
                logger.debug("Cache hit for hot viewed question: {}", questionId);
                return objectMapper.readValue(cached.toString(), Question.class);
            }

            logger.debug("Cache miss for hot viewed question: {}", questionId);
            return null;
        } catch (Exception e) {
            logger.error("Error getting hot viewed question from cache: {}", e.getMessage());
            return null;
        }
    }

    public List<String> getTopViewedQuestionIds(int limit) {
        try {
            Set<Object> topViewed = redisTemplate.opsForZSet().reverseRange(
                    "question:hot:viewed:ranking", 0, limit - 1);

            if (topViewed == null || topViewed.isEmpty()) {
                logger.info("No hot viewed questions in cache");
                return new ArrayList<>();
            }

            return topViewed.stream()
                    .map(obj -> obj.toString())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting top viewed question IDs: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public void invalidateHotViewCache(String questionId) {
        String key = HOT_VIEW_CACHE_KEY + questionId;
        redisTemplate.delete(key);
        redisTemplate.opsForZSet().remove("question:hot:viewed:ranking", questionId);
        logger.debug("Invalidated hot view cache for question: {}", questionId);
    }

    @Scheduled(fixedRate = 1800000)
    public void syncHotViewedQuestionsFromDB() {
        try {
            logger.info("Syncing hot viewed questions from database");

            List<Question> topViewed = questionRepository.findTopViewed(PageRequest.of(0, 50));
            for (Question question : topViewed) {
                if (question.getViewCount() != null && question.getViewCount() > 10) {
                    cacheHotViewedQuestion(question);
                }
            }

            logger.info("Synced {} hot viewed questions to cache", topViewed.size());
        } catch (Exception e) {
            logger.error("Error syncing hot viewed questions: {}", e.getMessage());
        }
    }

    public void recordUserActivity(String userId, String activityType) {
        try {
            String key = USER_ACTIVITY_KEY + userId;
            long timestamp = System.currentTimeMillis();
            String activityRecord = activityType + ":" + timestamp;

            redisTemplate.opsForList().rightPush(key, activityRecord);
            redisTemplate.expire(key, 7, TimeUnit.DAYS);

            redisTemplate.opsForZSet().incrementScore("user:activity:ranking", userId, 1);

            logger.debug("Recorded user activity: {} - {}", userId, activityType);
        } catch (Exception e) {
            logger.error("Error recording user activity: {}", e.getMessage());
        }
    }

    public void markUserOnline(String userId) {
        try {
            redisTemplate.opsForValue().set(ONLINE_USERS_KEY + ":" + userId, "online", Duration.ofMinutes(5));
            logger.debug("Marked user as online: {}", userId);
        } catch (Exception e) {
            logger.error("Error marking user online: {}", e.getMessage());
        }
    }

    public Long getOnlineUserCount() {
        try {
            Set<String> onlineKeys = redisTemplate.keys(ONLINE_USERS_KEY + ":*");
            return onlineKeys != null ? (long) onlineKeys.size() : 0L;
        } catch (Exception e) {
            logger.error("Error getting online user count: {}", e.getMessage());
            return 0L;
        }
    }

    public List<Map<String, Object>> getTopActiveUsers(int limit) {
        try {
            Set<ZSetOperations.TypedTuple<Object>> activeUsers =
                    redisTemplate.opsForZSet().reverseRangeWithScores("user:activity:ranking", 0, limit - 1);

            List<Map<String, Object>> result = new ArrayList<>();
            if (activeUsers != null) {
                for (ZSetOperations.TypedTuple<Object> tuple : activeUsers) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("userId", tuple.getValue());
                    user.put("activityScore", tuple.getScore().longValue());
                    result.add(user);
                }
            }
            return result;
        } catch (Exception e) {
            logger.error("Error getting top active users: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> getUserRecentActivity(String userId, int limit) {
        try {
            String key = USER_ACTIVITY_KEY + userId;
            List<Object> activities = redisTemplate.opsForList().range(key, -limit, -1);

            List<Map<String, Object>> result = new ArrayList<>();
            if (activities != null) {
                for (Object activity : activities) {
                    String[] parts = activity.toString().split(":");
                    if (parts.length >= 2) {
                        Map<String, Object> record = new HashMap<>();
                        record.put("type", parts[0]);
                        record.put("timestamp", Long.parseLong(parts[1]));
                        result.add(record);
                    }
                }
            }
            return result;
        } catch (Exception e) {
            logger.error("Error getting user recent activity: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Scheduled(fixedRate = 86400000)
    public void cleanupExpiredData() {
        try {
            logger.info("Cleaning up expired Redis data");

            Set<String> userActivityKeys = redisTemplate.keys(USER_ACTIVITY_KEY + "*");
            if (userActivityKeys != null) {
                for (String key : userActivityKeys) {
                    Long size = redisTemplate.opsForList().size(key);
                    if (size != null && size > 1000) {
                        redisTemplate.opsForList().trim(key, -500, -1);
                    }
                }
            }

            logger.info("Cleanup completed");
        } catch (Exception e) {
            logger.error("Error during cleanup: {}", e.getMessage());
        }
    }
}

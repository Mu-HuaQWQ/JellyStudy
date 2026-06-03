package com.jellystudy.controller;

import com.jellystudy.entity.ApiResponse;
import com.jellystudy.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/redis")
@CrossOrigin(origins = "*")
public class RedisController {

    @Autowired
    private RedisService redisService;

    @GetMapping("/popular-ranking")
    public ApiResponse<List<Map<String, Object>>> getPopularQuestionsRanking(
            @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> ranking = redisService.getPopularQuestionsRanking(limit);
        return ApiResponse.success(ranking);
    }

    @GetMapping("/hot-viewed/{questionId}")
    public ApiResponse<Map<String, Object>> getHotViewedQuestion(@PathVariable String questionId) {
        var cachedQuestion = redisService.getHotViewedQuestionFromCache(questionId);
        if (cachedQuestion != null) {
            Map<String, Object> result = Map.of(
                    "question", cachedQuestion,
                    "fromCache", true
            );
            return ApiResponse.success(result);
        }
        return ApiResponse.error(404, "Question not found in cache");
    }

    @GetMapping("/top-viewed-ids")
    public ApiResponse<List<String>> getTopViewedQuestionIds(
            @RequestParam(defaultValue = "10") int limit) {
        List<String> questionIds = redisService.getTopViewedQuestionIds(limit);
        return ApiResponse.success(questionIds);
    }

    @PostMapping("/sync/popular")
    public ApiResponse<Void> syncPopularQuestions() {
        redisService.syncPopularQuestionsFromDB();
        return ApiResponse.success();
    }

    @PostMapping("/sync/hot-viewed")
    public ApiResponse<Void> syncHotViewedQuestions() {
        redisService.syncHotViewedQuestionsFromDB();
        return ApiResponse.success();
    }

    @GetMapping("/users/online/count")
    public ApiResponse<Long> getOnlineUserCount() {
        Long count = redisService.getOnlineUserCount();
        return ApiResponse.success(count);
    }

    @GetMapping("/users/active/top")
    public ApiResponse<List<Map<String, Object>>> getTopActiveUsers(
            @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> users = redisService.getTopActiveUsers(limit);
        return ApiResponse.success(users);
    }

    @PostMapping("/users/{userId}/activity")
    public ApiResponse<Void> recordUserActivity(
            @PathVariable String userId,
            @RequestParam String activityType) {
        redisService.recordUserActivity(userId, activityType);
        return ApiResponse.success();
    }

    @GetMapping("/users/{userId}/activity/recent")
    public ApiResponse<List<Map<String, Object>>> getUserRecentActivity(
            @PathVariable String userId,
            @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> activities = redisService.getUserRecentActivity(userId, limit);
        return ApiResponse.success(activities);
    }

    @PostMapping("/users/{userId}/online")
    public ApiResponse<Void> markUserOnline(@PathVariable String userId) {
        redisService.markUserOnline(userId);
        return ApiResponse.success();
    }
}

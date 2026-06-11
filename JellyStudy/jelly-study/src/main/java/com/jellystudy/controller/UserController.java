package com.jellystudy.controller;

import com.jellystudy.entity.*;
import com.jellystudy.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.jellystudy.repository.AnswerRepository;
import com.jellystudy.repository.BrowseHistoryRepository;
import com.jellystudy.repository.QuestionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserCenterService userCenterService;

    @Autowired
    private AvatarStorageService avatarStorageService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private BrowseHistoryRepository browseHistoryRepository;

    @PostMapping
    public ApiResponse<User> createUser(@RequestBody @Validated User user) {
        if (userService.existsByUsername(user.getUsername())) {
            return ApiResponse.error(400, "用户名已存在");
        }
        User created = userService.create(user);
        created.setPassword(null);
        return ApiResponse.success(created);
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getUser(@PathVariable String id) {
        return userService.findById(id)
                .map(u -> {
                    u.setPassword(null);
                    return ApiResponse.success(u);
                })
                .orElse(ApiResponse.error(404, "用户不存在"));
    }

    @GetMapping("/username/{username}")
    public ApiResponse<User> getUserByUsername(@PathVariable String username) {
        return userService.findByUsername(username)
                .map(u -> {
                    u.setPassword(null);
                    return ApiResponse.success(u);
                })
                .orElse(ApiResponse.error(404, "用户不存在"));
    }

    @GetMapping
    public ApiResponse<List<User>> getAllUsers() {
        List<User> users = userService.findAll();
        users.forEach(u -> u.setPassword(null));
        return ApiResponse.success(users);
    }

    @PutMapping("/{id}")
    public ApiResponse<User> updateUser(
            @PathVariable String id,
            @RequestBody User user) {
        user.setId(id);
        User updated = userService.update(user);
        updated.setPassword(null);
        return ApiResponse.success(updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable String id) {
        userService.delete(id);
        return ApiResponse.success();
    }

    @GetMapping("/count")
    public ApiResponse<Long> getUserCount() {
        Long count = userService.count();
        return ApiResponse.success(count);
    }

    @PostMapping("/login")
    public ApiResponse<User> login(@RequestBody User loginRequest) {
        Optional<User> userOpt = userService.findByUsername(loginRequest.getUsername());
        if (userOpt.isEmpty()) {
            return ApiResponse.error(404, "用户不存在");
        }
        User user = userOpt.get();
        if (user.getPassword() != null && user.getPassword().equals(loginRequest.getPassword())) {
            user.setPassword(null);
            return ApiResponse.success(user);
        }
        return ApiResponse.error(401, "密码错误");
    }

    // ============ 头像上传 ============

    @PostMapping("/{id}/avatar")
    public ApiResponse<Map<String, String>> uploadAvatar(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {
        try {
            Optional<User> userOpt = userService.findById(id);
            if (userOpt.isEmpty()) {
                return ApiResponse.error(404, "用户不存在");
            }
            String path = avatarStorageService.store(id, file);
            User user = userOpt.get();
            user.setAvatar(path);
            userService.update(user);
            return ApiResponse.success(java.util.Collections.singletonMap("avatar", path));
        } catch (Exception e) {
            return ApiResponse.error("头像上传失败: " + e.getMessage());
        }
    }

    // ============ 关注 ============

    @PostMapping("/{id}/follow")
    public ApiResponse<Void> follow(@PathVariable String id, @RequestParam String targetId) {
        try {
            userCenterService.follow(id, targetId);
            return ApiResponse.success();
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/follow")
    public ApiResponse<Void> unfollow(@PathVariable String id, @RequestParam String targetId) {
        userCenterService.unfollow(id, targetId);
        return ApiResponse.success();
    }

    @GetMapping("/{id}/following")
    public ApiResponse<List<User>> getFollowing(@PathVariable String id) {
        return ApiResponse.success(userCenterService.getFollowing(id));
    }

    @GetMapping("/{id}/followers")
    public ApiResponse<List<User>> getFollowers(@PathVariable String id) {
        return ApiResponse.success(userCenterService.getFollowers(id));
    }

    @GetMapping("/{id}/follow-status")
    public ApiResponse<Boolean> followStatus(@PathVariable String id, @RequestParam String targetId) {
        return ApiResponse.success(userCenterService.isFollowing(id, targetId));
    }

    // ============ 收藏 ============

    @PostMapping("/{id}/favorites")
    public ApiResponse<Void> addFavorite(@PathVariable String id, @RequestParam String questionId) {
        userCenterService.addFavorite(id, questionId);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}/favorites")
    public ApiResponse<Void> removeFavorite(@PathVariable String id, @RequestParam String questionId) {
        userCenterService.removeFavorite(id, questionId);
        return ApiResponse.success();
    }

    @GetMapping("/{id}/favorites")
    public ApiResponse<List<Favorite>> getFavorites(@PathVariable String id) {
        return ApiResponse.success(userCenterService.getFavorites(id));
    }

    @GetMapping("/{id}/favorites/status")
    public ApiResponse<Boolean> favoriteStatus(@PathVariable String id, @RequestParam String questionId) {
        return ApiResponse.success(userCenterService.isFavorited(id, questionId));
    }

    // ============ 最近浏览 ============

    @PostMapping("/{id}/history")
    public ApiResponse<Void> recordHistory(@PathVariable String id, @RequestParam String questionId) {
        userCenterService.recordHistory(id, questionId);
        return ApiResponse.success();
    }

    @GetMapping("/{id}/history")
    public ApiResponse<List<BrowseHistory>> getHistory(@PathVariable String id) {
        return ApiResponse.success(userCenterService.getHistory(id));
    }

    // ============ 称号 ============

    @GetMapping("/{id}/titles")
    public ApiResponse<List<Map<String, Object>>> getTitles(@PathVariable String id) {
        return ApiResponse.success(userCenterService.getTitles(id));
    }

    @PutMapping("/{id}/display-title")
    public ApiResponse<User> setDisplayTitle(@PathVariable String id, @RequestParam String code) {
        try {
            return ApiResponse.success(userCenterService.setDisplayTitle(id, code));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // ============ 个人资料汇总 ============

    @GetMapping("/{id}/profile")
    public ApiResponse<Map<String, Object>> getProfile(@PathVariable String id) {
        try {
            return ApiResponse.success(userCenterService.getProfile(id));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 学习报告：统计用户在指定时段内的学习数据
     */
    @GetMapping("/{userId}/report")
    public ApiResponse<Map<String, Object>> getLearningReport(
            @PathVariable String userId,
            @RequestParam(defaultValue = "weekly") String type) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start;
        String periodName;
        if ("monthly".equals(type)) {
            start = now.minusDays(30);
            periodName = "本月";
        } else if ("yearly".equals(type)) {
            start = now.minusDays(365);
            periodName = "本年";
        } else {
            start = now.minusDays(7);
            periodName = "本周";
        }

        // 提问数
        long questionsAsked = questionRepository.countByAuthorIdAndCreateTimeAfter(userId, start);

        // 回答数
        long answersGiven = answerRepository.countByAuthorIdAndCreateTimeAfter(userId, start);

        // 获赞数（该时段内回答的点赞数之和）
        List<Answer> answers = answerRepository.findLikedByAuthorIdAndCreateTimeAfter(userId, start);
        long likesReceived = answers.stream()
                .filter(a -> a.getLikedByUsers() != null)
                .mapToLong(a -> a.getLikedByUsers().size())
                .sum();

        // 问题获赞数
        List<Question> questions = questionRepository.findByAuthorIdAndCreateTimeAfter(userId, start);
        long questionLikes = questions.stream()
                .filter(q -> q.getLikedByUsers() != null)
                .mapToLong(q -> q.getLikedByUsers().size())
                .sum();
        likesReceived += questionLikes;

        // 最晚在线时间（从浏览记录取最后一条）
        String latestOnlineTime = "暂无";
        List<BrowseHistory> histories = browseHistoryRepository.findByUserIdOrderByViewTimeDesc(userId);
        if (histories != null && !histories.isEmpty()) {
            LocalDateTime latest = histories.get(0).getViewTime();
            if (latest != null && latest.isAfter(start)) {
                latestOnlineTime = latest.toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            }
        }

        // 活跃天数
        Set<String> activeDays = new java.util.HashSet<>();
        questions.forEach(q -> { if (q.getCreateTime() != null) activeDays.add(q.getCreateTime().toLocalDate().toString()); });
        answers.forEach(a -> { if (a.getCreateTime() != null) activeDays.add(a.getCreateTime().toLocalDate().toString()); });

        // 该时段获得的称号（简化：检查用户当前称号）
        java.util.Optional<User> userOpt = userService.findById(userId);
        List<String> earnedTitles = new java.util.ArrayList<>();
        String title = null;
        if (userOpt.isPresent()) {
            title = userOpt.get().getDisplayTitle();
            if (title != null && !title.isEmpty()) earnedTitles.add(title);
        }

        // 题库正确数（可选—如果 question_bank_items 有答题记录则统计）
        long correctQuizAnswers = 0;

        // 日期范围
        java.time.format.DateTimeFormatter dateFmt = java.time.format.DateTimeFormatter.ofPattern("M.d");
        String dateRange = start.format(dateFmt) + " - " + now.format(dateFmt);

        Map<String, Object> report = new java.util.LinkedHashMap<>();
        report.put("period", periodName);
        report.put("dateRange", dateRange);
        report.put("questionsAsked", questionsAsked);
        report.put("answersGiven", answersGiven);
        report.put("likesReceived", likesReceived);
        report.put("latestOnlineTime", latestOnlineTime);
        report.put("activeDays", activeDays.size());
        report.put("earnedTitles", earnedTitles);
        report.put("correctQuizAnswers", correctQuizAnswers);

        return ApiResponse.success(report);
    }
}

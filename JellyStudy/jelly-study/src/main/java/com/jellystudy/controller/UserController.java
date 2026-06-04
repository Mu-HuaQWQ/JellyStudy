package com.jellystudy.controller;

import com.jellystudy.entity.*;
import com.jellystudy.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
}

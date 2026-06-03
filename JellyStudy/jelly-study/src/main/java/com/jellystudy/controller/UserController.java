package com.jellystudy.controller;

import com.jellystudy.entity.*;
import com.jellystudy.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

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
}

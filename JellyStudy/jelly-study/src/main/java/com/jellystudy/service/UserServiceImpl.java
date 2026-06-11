package com.jellystudy.service;

import com.jellystudy.entity.User;
import com.jellystudy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User create(User user) {
        if (user.getCreateTime() == null) {
            user.setCreateTime(LocalDateTime.now());
        }
        if (user.getReputation() == null) {
            user.setReputation(0);
        }
        if (user.getQuestionCount() == null) {
            user.setQuestionCount(0);
        }
        if (user.getAnswerCount() == null) {
            user.setAnswerCount(0);
        }
        if (user.getRole() == null) {
            user.setRole("USER");
        }
        if (user.getFollowingCount() == null) {
            user.setFollowingCount(0);
        }
        if (user.getFollowerCount() == null) {
            user.setFollowerCount(0);
        }
        if (user.getOwnedTitles() == null) {
            user.setOwnedTitles(new ArrayList<>(java.util.Collections.singletonList("newbie")));
        }
        if (user.getDisplayTitle() == null) {
            user.setDisplayTitle("newbie");
        }
        if (user.getCreditPoints() == null) {
            user.setCreditPoints(0);
        }
        if (user.getTotalSpent() == null) {
            user.setTotalSpent(0);
        }
        if (user.getLevel() == null) {
            user.setLevel(0);
        }
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User update(User user) {
        return userRepository.save(user);
    }

    @Override
    public void delete(String id) {
        userRepository.deleteById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public Long count() {
        return userRepository.count();
    }
}

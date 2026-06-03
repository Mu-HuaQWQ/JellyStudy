package com.jellystudy.service;

import com.jellystudy.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    
    User create(User user);
    
    Optional<User> findById(String id);
    
    Optional<User> findByUsername(String username);
    
    List<User> findAll();
    
    User update(User user);
    
    void delete(String id);
    
    boolean existsByUsername(String username);
    
    Long count();
}

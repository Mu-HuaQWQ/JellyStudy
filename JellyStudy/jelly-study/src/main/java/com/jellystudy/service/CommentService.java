package com.jellystudy.service;

import com.jellystudy.entity.Comment;
import com.jellystudy.entity.CommentRequest;

import java.util.List;
import java.util.Optional;

public interface CommentService {
    
    Comment create(CommentRequest commentRequest, String authorId, String authorName);
    
    Optional<Comment> findById(String id);
    
    List<Comment> findByTargetId(String targetId);
    
    List<Comment> findByParentCommentId(String parentCommentId);
    
    Comment update(String id, Comment comment);
    
    void delete(String id);
    
    Long count();
    
    Comment likeComment(String commentId, String userId);
    
    Comment unlikeComment(String commentId, String userId);
    
    List<Comment> findByTargetIdAndType(String targetId, com.jellystudy.entity.Comment.CommentType commentType);
}

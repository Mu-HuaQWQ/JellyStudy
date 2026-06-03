package com.jellystudy.service;

import com.jellystudy.entity.Comment;
import com.jellystudy.entity.Comment.CommentType;
import com.jellystudy.entity.CommentRequest;
import com.jellystudy.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Override
    public Comment create(CommentRequest commentRequest, String authorId, String authorName) {
        Comment comment = Comment.builder()
                .targetId(commentRequest.getTargetId())
                .commentType(commentRequest.getCommentType())
                .parentCommentId(commentRequest.getParentCommentId())
                .content(commentRequest.getContent())
                .authorId(authorId)
                .authorName(authorName)
                .likedByUsers(new HashSet<>())
                .likeCount(0)
                .isDeleted(false)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        return commentRepository.save(comment);
    }

    @Override
    public Optional<Comment> findById(String id) {
        return commentRepository.findById(id);
    }

    @Override
    public List<Comment> findByTargetId(String targetId) {
        return commentRepository.findByTargetIdAndTypeNotDeleted(targetId, null);
    }

    @Override
    public List<Comment> findByParentCommentId(String parentCommentId) {
        return commentRepository.findByParentCommentId(parentCommentId);
    }

    @Override
    public Comment update(String id, Comment comment) {
        Optional<Comment> existingOpt = commentRepository.findById(id);
        if (existingOpt.isPresent()) {
            Comment existing = existingOpt.get();
            if (comment.getContent() != null) {
                existing.setContent(comment.getContent());
            }
            existing.setUpdateTime(LocalDateTime.now());
            return commentRepository.save(existing);
        }
        throw new RuntimeException("Comment not found with id: " + id);
    }

    @Override
    public void delete(String id) {
        Optional<Comment> commentOpt = commentRepository.findById(id);
        commentOpt.ifPresent(comment -> {
            comment.setIsDeleted(true);
            comment.setUpdateTime(LocalDateTime.now());
            commentRepository.save(comment);
        });
    }

    @Override
    public Long count() {
        return commentRepository.count();
    }

    @Override
    public Comment likeComment(String commentId, String userId) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isPresent()) {
            Comment comment = commentOpt.get();
            if (comment.getLikedByUsers() == null) {
                comment.setLikedByUsers(new HashSet<>());
            }
            if (!comment.getLikedByUsers().contains(userId)) {
                comment.getLikedByUsers().add(userId);
                comment.setLikeCount(comment.getLikedByUsers().size());
                comment.setUpdateTime(LocalDateTime.now());
                return commentRepository.save(comment);
            }
            return comment;
        }
        throw new RuntimeException("Comment not found with id: " + commentId);
    }

    @Override
    public Comment unlikeComment(String commentId, String userId) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isPresent()) {
            Comment comment = commentOpt.get();
            if (comment.getLikedByUsers() != null && comment.getLikedByUsers().contains(userId)) {
                comment.getLikedByUsers().remove(userId);
                comment.setLikeCount(comment.getLikedByUsers().size());
                comment.setUpdateTime(LocalDateTime.now());
                return commentRepository.save(comment);
            }
            return comment;
        }
        throw new RuntimeException("Comment not found with id: " + commentId);
    }

    @Override
    public List<Comment> findByTargetIdAndType(String targetId, CommentType commentType) {
        return commentRepository.findByTargetIdAndTypeNotDeleted(targetId, commentType);
    }
}

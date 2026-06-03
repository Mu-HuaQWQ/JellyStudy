package com.jellystudy.controller;

import com.jellystudy.entity.*;
import com.jellystudy.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "*")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping
    public ApiResponse<Comment> createComment(
            @RequestBody @Validated CommentRequest commentRequest,
            @RequestParam(defaultValue = "user001") String authorId,
            @RequestParam(defaultValue = "Anonymous") String authorName) {
        Comment comment = commentService.create(commentRequest, authorId, authorName);
        return ApiResponse.success(comment);
    }

    @GetMapping("/{id}")
    public ApiResponse<Comment> getComment(@PathVariable String id) {
        return commentService.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "Comment not found"));
    }

    @GetMapping("/target/{targetId}")
    public ApiResponse<List<Comment>> getCommentsByTarget(@PathVariable String targetId) {
        List<Comment> comments = commentService.findByTargetId(targetId);
        return ApiResponse.success(comments);
    }

    @GetMapping("/target/{targetId}/type/{commentType}")
    public ApiResponse<List<Comment>> getCommentsByTargetAndType(
            @PathVariable String targetId,
            @PathVariable Comment.CommentType commentType) {
        List<Comment> comments = commentService.findByTargetIdAndType(targetId, commentType);
        return ApiResponse.success(comments);
    }

    @GetMapping("/parent/{parentCommentId}")
    public ApiResponse<List<Comment>> getReplies(@PathVariable String parentCommentId) {
        List<Comment> comments = commentService.findByParentCommentId(parentCommentId);
        return ApiResponse.success(comments);
    }

    @GetMapping("/count")
    public ApiResponse<Long> getCommentCount() {
        Long count = commentService.count();
        return ApiResponse.success(count);
    }

    @PutMapping("/{id}")
    public ApiResponse<Comment> updateComment(
            @PathVariable String id,
            @RequestBody Comment comment) {
        try {
            Comment updated = commentService.update(id, comment);
            return ApiResponse.success(updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteComment(@PathVariable String id) {
        commentService.delete(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/like")
    public ApiResponse<Comment> likeComment(
            @PathVariable String id,
            @RequestParam(defaultValue = "user001") String userId) {
        try {
            Comment comment = commentService.likeComment(id, userId);
            return ApiResponse.success(comment);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/unlike")
    public ApiResponse<Comment> unlikeComment(
            @PathVariable String id,
            @RequestParam(defaultValue = "user001") String userId) {
        try {
            Comment comment = commentService.unlikeComment(id, userId);
            return ApiResponse.success(comment);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}

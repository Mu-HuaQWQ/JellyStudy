package com.jellystudy.repository;

import com.jellystudy.entity.Comment;
import com.jellystudy.entity.Comment.CommentType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {
    
    List<Comment> findByTargetId(String targetId);
    
    @Query("{ 'targetId': ?0, 'commentType': ?1, 'isDeleted': false }")
    List<Comment> findByTargetIdAndTypeNotDeleted(String targetId, CommentType commentType);
    
    List<Comment> findByParentCommentId(String parentCommentId);
    
    @Query("{ 'authorId': ?0 }")
    List<Comment> findByAuthorId(String authorId);
    
    @Query("{ 'commentType': ?0, 'isDeleted': false }")
    List<Comment> findByCommentType(CommentType commentType);
}

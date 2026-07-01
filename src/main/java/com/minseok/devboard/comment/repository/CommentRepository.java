package com.minseok.devboard.comment.repository;

import com.minseok.devboard.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}

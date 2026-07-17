package com.minseok.devboard.comment.repository;

import com.minseok.devboard.board.entity.Board;
import com.minseok.devboard.comment.entity.Comment;
import com.minseok.devboard.comment.entity.CommentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    Optional<Comment> findByIdAndStatus(Long commentId, CommentStatus status);
    
    List<Comment> findByBoardOrderByIdAsc(Board board);
}

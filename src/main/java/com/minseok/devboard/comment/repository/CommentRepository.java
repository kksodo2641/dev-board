package com.minseok.devboard.comment.repository;

import com.minseok.devboard.board.entity.Board;
import com.minseok.devboard.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    List<Comment> findByBoardOrderByIdAsc(Board board);
}

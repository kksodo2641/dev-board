package com.minseok.devboard.board.repository;

import com.minseok.devboard.board.entity.Board;
import com.minseok.devboard.board.entity.BoardStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {
    
    Optional<Board> findByIdAndStatus(Long id, BoardStatus status);
    
    List<Board> findAllByOrderByIdDesc();
}

















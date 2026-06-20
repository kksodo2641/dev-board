package com.minseok.devboard.board.repository;

import com.minseok.devboard.board.entity.Board;
import com.minseok.devboard.board.entity.BoardStatus;
import com.minseok.devboard.board.repository.paging.BoardPagingRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends
        JpaRepository<Board, Long>,
        BoardPagingRepository {
    
    Optional<Board> findByIdAndStatus(Long id, BoardStatus status);
    
    List<Board> findAllByOrderByIdDesc();
}

















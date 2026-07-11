package com.minseok.devboard.board.repository.paging;

import com.minseok.devboard.board.entity.Board;

import java.util.List;

public interface BoardPagingRepository {
    
    long countBoards();
    
    List<Board> findBoardList(int offset, int limit);
}

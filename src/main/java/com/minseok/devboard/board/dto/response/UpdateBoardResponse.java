package com.minseok.devboard.board.dto.response;

import com.minseok.devboard.board.entity.Board;
import com.minseok.devboard.board.entity.BoardCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class UpdateBoardResponse {
    
    private final Long boardId;
    private final String title;
    private final String content;
    private final BoardCategory category;
    
    public static UpdateBoardResponse toResponse(final Board board) {
        assert (board != null);
        
        return new UpdateBoardResponse(board.getId(),
                                       board.getTitle(),
                                       board.getContent(),
                                       board.getCategory());
    }
}

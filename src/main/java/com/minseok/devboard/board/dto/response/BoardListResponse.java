package com.minseok.devboard.board.dto.response;

import com.minseok.devboard.board.entity.Board;
import com.minseok.devboard.board.entity.BoardCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class BoardListResponse {
    
    private final Long boardId;
    private final String title;
    private final String writerNickname;
    private final BoardCategory category;
    private final LocalDateTime createdAt;
    
    private final boolean deleted;
    
    public static BoardListResponse toResponse(final Board board) {
        assert (board != null);
        
        final String title = board.isDeleted()
                             ? "삭제된 게시글입니다."
                             : board.getTitle();
        
        return new BoardListResponse(board.getId(),
                                     title,
                                     board.getWriter().getNickname(),
                                     board.getCategory(),
                                     board.getCreatedAt(),
                                     board.isDeleted());
    }
}


















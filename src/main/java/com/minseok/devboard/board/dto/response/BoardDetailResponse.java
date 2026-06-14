package com.minseok.devboard.board.dto.response;

import com.minseok.devboard.board.entity.Board;
import com.minseok.devboard.board.entity.BoardCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class BoardDetailResponse {
    
    private Long boardId;               // 게시글 ID
    private String writerNickname;      // 작성자 닉네임
    private String title;               // 게시글 제목
    private String content;             // 게시글 내용
    private BoardCategory category;     // 게시글 카테고리
    private LocalDateTime createdAt;    // 작성 일시
    
    public static BoardDetailResponse toResponse(final Board board) {
        assert (board != null);
        
        return new BoardDetailResponse(board.getId(),
                                       board.getWriter().getNickname(),
                                       board.getTitle(),
                                       board.getContent(),
                                       board.getCategory(),
                                       board.getCreatedAt());
    }
}


















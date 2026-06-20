package com.minseok.devboard.board.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class BoardPageResponse {
    
    private final List<BoardListResponse> boardList; // 현재 페이지 게시글 목록
    private final int currentPage;                   // 현재 페이지 (1-base)
    private final int totalPages;                    // 전체 페이지 수
    private final long totalCount;                   // 전체 게시글 수
    private final int startPage;                     // block 시작 페이지  (1-base)
    private final int endPage;                       // block 마지막 페이지 (1-base)
    
    @Accessors(fluent = true)
    private final boolean hasPrevious;               // 이전 페이지 존재 여부
    
    @Accessors(fluent = true)
    private final boolean hasNext;                   // 다음 페이지 존재 여부
    
    public static BoardPageResponse of(final List<BoardListResponse> boardList,
                                       final int currentPage,
                                       final int totalPages,
                                       final long totalCount,
                                       final int startPage,
                                       final int endPage,
                                       final boolean hasPrevious,
                                       final boolean hasNext) {
        return new BoardPageResponse(boardList,
                                     currentPage,
                                     totalPages,
                                     totalCount,
                                     startPage,
                                     endPage,
                                     hasPrevious,
                                     hasNext);
    }
}

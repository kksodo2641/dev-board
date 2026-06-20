package com.minseok.devboard.board.service;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class BoardPagingUtils {
    
    public static final int MIN_PAGE = 1;   // 게시글이 없을 때도 1페이지로 보정
    public static final int PAGE_SIZE = 15;
    public static final int BLOCK_SIZE = 5;
    
    public static int getTotalPages(final long totalCount) {
        assert (totalCount >= 0);
        return Math.max(MIN_PAGE,
                        (int) Math.ceil(totalCount / (double) PAGE_SIZE));
    }
    
    public static int getCurrentPage(final int page,
                                     final int totalPages) {
        assert (totalPages >= MIN_PAGE);
        return Math.max(MIN_PAGE,
                        Math.min(totalPages, page));
    }
    
    public static int getStartPage(final int currentPage) {
        assert (currentPage >= MIN_PAGE);
        
        final int blockIndex = (currentPage - 1) / BLOCK_SIZE; // 0-base
        return blockIndex * BLOCK_SIZE + 1;
    }
    
    public static int getEndPage(final int startPage,
                                 final int totalPages) {
        assert (startPage >= MIN_PAGE);
        assert (totalPages >= startPage);
        
        return Math.min(totalPages,
                        startPage + BLOCK_SIZE - 1);
    }
}

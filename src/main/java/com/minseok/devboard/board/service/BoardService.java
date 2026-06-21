package com.minseok.devboard.board.service;

import com.minseok.devboard.board.dto.request.WriteBoardRequest;
import com.minseok.devboard.board.dto.response.BoardDetailResponse;
import com.minseok.devboard.board.dto.response.BoardListResponse;
import com.minseok.devboard.board.dto.response.BoardPageResponse;
import com.minseok.devboard.board.entity.Board;
import com.minseok.devboard.board.entity.BoardCategory;
import com.minseok.devboard.board.entity.BoardStatus;
import com.minseok.devboard.board.exception.BoardNotFoundException;
import com.minseok.devboard.board.repository.BoardRepository;
import com.minseok.devboard.global.exception.AccessDeniedException;
import com.minseok.devboard.member.entity.Member;
import com.minseok.devboard.member.entity.MemberStatus;
import com.minseok.devboard.member.exception.MemberNotFoundException;
import com.minseok.devboard.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.minseok.devboard.board.service.BoardPagingUtils.MIN_PAGE;
import static com.minseok.devboard.board.service.BoardPagingUtils.PAGE_SIZE;
import static com.minseok.devboard.board.service.BoardPagingUtils.getCurrentPage;
import static com.minseok.devboard.board.service.BoardPagingUtils.getEndPage;
import static com.minseok.devboard.board.service.BoardPagingUtils.getStartPage;
import static com.minseok.devboard.board.service.BoardPagingUtils.getTotalPages;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BoardService {
    
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    
    @Transactional
    public Long writeBoard(final Long memberId,
                           final WriteBoardRequest request) {
        assert (memberId != null);
        assert (request != null);
        
        final Member writer = memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)
                                              .orElseThrow(MemberNotFoundException::new);
        
        if (!request.getCategory()
                    .canWrite(writer.getRole())) {
            throw new AccessDeniedException();
        }
        
        final Board board = Board.create(writer,
                                         request.getTitle(),
                                         request.getContent(),
                                         request.getCategory());
        
        return boardRepository.save(board)
                              .getId();
    }
    
    /**
     * 상세 조회
     */
    public BoardDetailResponse readBoard(final Long boardId) {
        assert (boardId != null);
        
        return boardRepository.findByIdAndStatus(boardId, BoardStatus.ACTIVE)
                              .map(BoardDetailResponse::toResponse)
                              .orElseThrow(BoardNotFoundException::new);
    }
    
    /**
     * @param page 1-base
     */
    public BoardPageResponse getBoardPage(final int page) {
        final long totalCount = boardRepository.countBoards();
        
        final int totalPages = getTotalPages(totalCount);
        final int currentPage = getCurrentPage(page, totalPages); // 1-base
        final int startPage = getStartPage(currentPage);          // 1-base
        final int endPage = getEndPage(startPage, totalPages);    // 1-base
        
        final int offset = (currentPage - 1) * PAGE_SIZE; // 0-base
        final List<BoardListResponse> boardList =
                boardRepository.findBoardList(offset, PAGE_SIZE).stream()
                               .map(BoardListResponse::toResponse)
                               .toList();
        
        return BoardPageResponse.of(
                boardList,
                currentPage,
                totalPages,
                totalCount,
                startPage,
                endPage,
                currentPage > MIN_PAGE,
                currentPage < totalPages
        );
    }
    
    public List<BoardCategory> getWritableCategories(final Long memberId) {
        assert (memberId != null);
        
        final Member member = memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)
                                              .orElseThrow(MemberNotFoundException::new);
        
        return member.isAdmin()
               ? List.of(BoardCategory.values())
               : BoardCategory.userCategories();
    }
}


















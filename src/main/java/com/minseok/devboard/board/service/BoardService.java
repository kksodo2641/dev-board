package com.minseok.devboard.board.service;

import com.minseok.devboard.board.dto.request.UpdateBoardRequest;
import com.minseok.devboard.board.dto.request.WriteBoardRequest;
import com.minseok.devboard.board.dto.response.BoardDetailResponse;
import com.minseok.devboard.board.dto.response.BoardListResponse;
import com.minseok.devboard.board.dto.response.BoardPageResponse;
import com.minseok.devboard.board.dto.response.UpdateBoardResponse;
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
        
        final Member member = findActiveMemberElseThrow(memberId);
        
        validateWritableCategory(member, request.getCategory());
        
        final Board board = Board.create(member,
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
        
        final Board board = findActiveBoardElseThrow(boardId);
        return BoardDetailResponse.toResponse(board);
    }
    
    /**
     * 페이지 조회
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
    
    public UpdateBoardResponse getBoardForUpdate(final Long memberId,
                                                 final Long boardId) {
        assert (memberId != null);
        assert (boardId != null);
        
        final Member member = findActiveMemberElseThrow(memberId);
        final Board board = findActiveBoardElseThrow(boardId);
        
        validateBoardWriter(member, board);
        
        return UpdateBoardResponse.toResponse(board);
    }
    
    @Transactional
    public void updateBoard(final Long memberId,
                            final Long boardId,
                            final UpdateBoardRequest request) {
        assert (memberId != null);
        assert (boardId != null);
        assert (request != null);
        
        final Member member = findActiveMemberElseThrow(memberId);
        final Board board = findActiveBoardElseThrow(boardId);
        
        validateBoardWriter(member, board);
        validateWritableCategory(member, request.getCategory());
        
        board.update(request.getTitle(),
                     request.getContent(),
                     request.getCategory());
    }
    
    public List<BoardCategory> getWritableCategories(final Long memberId) {
        assert (memberId != null);
        
        final Member member = findActiveMemberElseThrow(memberId);
        
        return member.isAdmin()
               ? List.of(BoardCategory.values())
               : BoardCategory.userCategories();
    }
    
    //== private method ==//
    
    /**
     * @throws MemberNotFoundException if no value present
     */
    private Member findActiveMemberElseThrow(final long memberId) {
        return memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)
                               .orElseThrow(MemberNotFoundException::new);
    }
    
    /**
     * @throws BoardNotFoundException if no value present
     */
    private Board findActiveBoardElseThrow(final long boardId) {
        return boardRepository.findByIdAndStatus(boardId, BoardStatus.ACTIVE)
                              .orElseThrow(BoardNotFoundException::new);
    }
    
    /**
     * @throws AccessDeniedException if member is not writer
     */
    private static void validateBoardWriter(final Member member, final Board board) {
        assert (member != null);
        assert (board != null);
        
        final long writerId = board.getWriter().getId();
        final long memberId = member.getId();
        
        if (writerId != memberId) {
            throw new AccessDeniedException();
        }
    }
    
    /**
     * @throws AccessDeniedException if category is NOTICE but, member is not admin
     */
    private static void validateWritableCategory(final Member member,
                                                 final BoardCategory category) {
        assert (member != null);
        assert (category != null);
        
        if (!category.canWrite(member)) {
            throw new AccessDeniedException();
        }
    }
}

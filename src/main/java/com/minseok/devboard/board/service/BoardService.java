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
    
    /**
     * 게시글 작성
     *
     * @throws MemberNotFoundException ACTIVE 회원이 존재하지 않는 경우
     * @throws AccessDeniedException 카테고리 작성 권한이 없는 경우
     */
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
     *
     * @throws BoardNotFoundException ACTIVE 게시글이 존재하지 않는 경우
     */
    public BoardDetailResponse readBoard(final Long boardId) {
        assert (boardId != null);
        
        final Board board = findActiveBoardElseThrow(boardId);
        return BoardDetailResponse.toResponse(board);
    }
    
    /**
     * 페이지 조회
     *
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
    
    /**
     * 게시글 수정 화면 조회
     *
     * @throws MemberNotFoundException ACTIVE 회원이 존재하지 않는 경우
     * @throws BoardNotFoundException ACTIVE 게시글이 존재하지 않는 경우
     * @throws AccessDeniedException 해당 게시글 작성자가 아닌 경우
     */
    public UpdateBoardResponse getBoardForUpdate(final Long memberId,
                                                 final Long boardId) {
        assert (memberId != null);
        assert (boardId != null);
        
        final Member member = findActiveMemberElseThrow(memberId);
        final Board board = findActiveBoardElseThrow(boardId);
        
        validateBoardWriter(member, board);
        
        return UpdateBoardResponse.toResponse(board);
    }
    
    /**
     * 게시글 수정
     *
     * @throws MemberNotFoundException ACTIVE 회원이 존재하지 않는 경우
     * @throws BoardNotFoundException ACTIVE 게시글이 존재하지 않는 경우
     * @throws AccessDeniedException 해당 게시글 작성자가 아니거나, 공지사항 수정 권한이 없는 경우
     */
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
    
    /**
     * 작성 가능한 카테고리 목록 조회
     *
     * @throws MemberNotFoundException ACTIVE 회원이 존재하지 않는 경우
     */
    public List<BoardCategory> getWritableCategories(final Long memberId) {
        assert (memberId != null);
        
        final Member member = findActiveMemberElseThrow(memberId);
        
        return member.isAdmin()
               ? List.of(BoardCategory.values())
               : BoardCategory.userCategories();
    }
    
    //== private method ==//
    
    /**
     * ACTIVE 회원 조회
     *
     * @throws MemberNotFoundException ACTIVE 회원이 존재하지 않는 경우
     */
    private Member findActiveMemberElseThrow(final long memberId) {
        return memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)
                               .orElseThrow(MemberNotFoundException::new);
    }
    
    /**
     * ACTIVE 게시글 조회
     *
     * @throws BoardNotFoundException ACTIVE 게시글이 존재하지 않는 경우
     */
    private Board findActiveBoardElseThrow(final long boardId) {
        return boardRepository.findByIdAndStatus(boardId, BoardStatus.ACTIVE)
                              .orElseThrow(BoardNotFoundException::new);
    }
    
    /**
     * 작성자 검증
     *
     * @throws AccessDeniedException 해당 게시글 작성자가 아닌 경우
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
     * 카테고리 권한 검증
     *
     * @throws AccessDeniedException 카테고리 권한이 없는 경우
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

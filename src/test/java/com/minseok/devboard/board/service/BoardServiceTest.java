package com.minseok.devboard.board.service;

import com.minseok.devboard.IntegrationTest;
import com.minseok.devboard.board.dto.request.UpdateBoardRequest;
import com.minseok.devboard.board.dto.request.WriteBoardRequest;
import com.minseok.devboard.board.dto.response.BoardDetailResponse;
import com.minseok.devboard.board.dto.response.BoardListResponse;
import com.minseok.devboard.board.dto.response.BoardPageResponse;
import com.minseok.devboard.board.dto.response.BoardUpdateResponse;
import com.minseok.devboard.board.entity.Board;
import com.minseok.devboard.board.entity.BoardCategory;
import com.minseok.devboard.board.entity.BoardStatus;
import com.minseok.devboard.board.exception.BoardNotFoundException;
import com.minseok.devboard.board.repository.BoardRepository;
import com.minseok.devboard.global.exception.AccessDeniedException;
import com.minseok.devboard.member.dto.request.SignupRequest;
import com.minseok.devboard.member.entity.Gender;
import com.minseok.devboard.member.entity.Member;
import com.minseok.devboard.member.entity.MemberStatus;
import com.minseok.devboard.member.exception.MemberNotFoundException;
import com.minseok.devboard.member.repository.MemberRepository;
import com.minseok.devboard.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;

import static com.minseok.devboard.board.entity.BoardCategory.FREE;
import static com.minseok.devboard.board.entity.BoardCategory.JOB;
import static com.minseok.devboard.board.entity.BoardCategory.NOTICE;
import static com.minseok.devboard.board.entity.BoardCategory.QNA;
import static com.minseok.devboard.board.service.BoardPagingUtils.PAGE_SIZE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoardServiceTest extends IntegrationTest {
    
    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    
    @Autowired BoardService boardService;
    @Autowired BoardRepository boardRepository;
    
    //==게시글 작성==//
    
    @Test
    @DisplayName("ACTIVE 회원은 일반 게시글을 작성할 수 있다.")
    void writeBoardByActiveMember() {
        // given
        final Long memberId = createUser("user@test.com", "user");
        
        final WriteBoardRequest writeBoardRequest = new WriteBoardRequest("title",
                                                                          "content",
                                                                          FREE);
        
        // when
        final Long boardId = boardService.writeBoard(memberId,
                                                     writeBoardRequest);
        
        // then
        final Board board = boardRepository.findByIdAndStatus(boardId, BoardStatus.ACTIVE)
                                           .orElseThrow();
        
        assertThat(board.getWriter().getId())
                .isEqualTo(memberId);
        
        assertThat(board.getTitle())
                .isEqualTo(writeBoardRequest.getTitle());
        
        assertThat(board.getContent())
                .isEqualTo(writeBoardRequest.getContent());
        
        assertThat(board.getCategory())
                .isEqualTo(writeBoardRequest.getCategory());
        
        assertThat(board.getStatus())
                .isEqualTo(BoardStatus.ACTIVE);
    }
    
    @Test
    @DisplayName("존재하지 않는 회원은 게시글을 작성할 수 없다.")
    void writeBoardByNotFoundMember() {
        // given
        final WriteBoardRequest writeBoardRequest = new WriteBoardRequest("title",
                                                                          "content",
                                                                          FREE);
        
        // when, then
        assertThatThrownBy(() -> boardService.writeBoard(Long.MAX_VALUE, writeBoardRequest))
                .isInstanceOf(MemberNotFoundException.class);
    }
    
    @Test
    @DisplayName("탈퇴한 회원은 게시글을 작성할 수 없다.")
    void writeBoardByWithdrawnMember() {
        // given
        final Long memberId = createUser("user@test.com", "user");
        
        final WriteBoardRequest writeBoardRequest = new WriteBoardRequest("title",
                                                                          "content",
                                                                          FREE);
        
        memberService.withdraw(memberId);
        
        // when, then
        assertThatThrownBy(() -> boardService.writeBoard(memberId, writeBoardRequest))
                .isInstanceOf(MemberNotFoundException.class);
    }
    
    @Test
    @DisplayName("일반 회원은 공지사항을 작성할 수 없다.")
    void writeNoticeByNormalMember() {
        // given
        final Long memberId = createUser("user@test.com", "user");
        
        final WriteBoardRequest writeBoardRequest = new WriteBoardRequest("title",
                                                                          "content",
                                                                          BoardCategory.NOTICE);
        
        // when, then
        assertThatThrownBy(() -> boardService.writeBoard(memberId, writeBoardRequest))
                .isInstanceOf(AccessDeniedException.class);
    }
    
    @Test
    @DisplayName("관리자는 공지사항을 작성할 수 있다.")
    void writeNoticeByAdminMember() {
        // given
        final Long memberId = getAdminMemberId();
        
        final WriteBoardRequest writeBoardRequest = new WriteBoardRequest("title",
                                                                          "content",
                                                                          BoardCategory.NOTICE);
        
        // when
        final Long boardId = boardService.writeBoard(memberId,
                                                     writeBoardRequest);
        
        // then
        final Board board = boardRepository.findByIdAndStatus(boardId, BoardStatus.ACTIVE)
                                           .orElseThrow();
        
        assertThat(board.getWriter().getId())
                .isEqualTo(memberId);
        
        assertThat(board.getTitle())
                .isEqualTo(writeBoardRequest.getTitle());
        
        assertThat(board.getContent())
                .isEqualTo(writeBoardRequest.getContent());
        
        assertThat(board.getCategory())
                .isEqualTo(BoardCategory.NOTICE);
        
        assertThat(board.getStatus())
                .isEqualTo(BoardStatus.ACTIVE);
    }
    
    //==게시글 조회==//
    
    @Test
    @DisplayName("게시글 정상 조회")
    void readBoard() {
        // given
        final Long memberId = createUser("user@test.com", "user");
        final Long boardId = createBoard(memberId, "title", "content", QNA);
        
        // when
        final BoardDetailResponse response = boardService.readBoard(boardId);
        
        // then
        final Board board = boardRepository.findByIdAndStatus(boardId, BoardStatus.ACTIVE)
                                           .orElseThrow(BoardNotFoundException::new);
        
        assertThat(response.getBoardId())
                .isEqualTo(board.getId());
        
        assertThat(response.getWriterNickname())
                .isEqualTo(board.getWriter().getNickname());
        
        assertThat(response.getTitle())
                .isEqualTo(board.getTitle());
        
        assertThat(response.getContent())
                .isEqualTo(board.getContent());
        
        assertThat(response.getCategory())
                .isEqualTo(board.getCategory());
        
        assertThat(response.getCreatedAt())
                .isEqualTo(board.getCreatedAt());
    }
    
    @Test
    @DisplayName("게시글 조회 실패 - 존재하지 않는 게시글")
    void readBoardWithNotFoundBoard() {
        // when, then
        assertThatThrownBy(() -> boardService.readBoard(Long.MAX_VALUE))
                .isInstanceOf(BoardNotFoundException.class);
    }
    
    @Test
    @DisplayName("게시글 조회 실패 - 삭제된 게시글")
    void readBoardWithDeletedBoard() {
        // given
        final Long memberId = createUser("user@test.com",
                                         "user");
        
        final Long boardId = createBoard(memberId, "title", "content", JOB);
        
        // 게시글 삭제
        final Board board = boardRepository.findById(boardId)
                                           .orElseThrow();
        board.delete();
        
        assertThat(board.getStatus()).isEqualTo(BoardStatus.DELETED);
        
        // when, then
        assertThatThrownBy(() -> boardService.readBoard(boardId))
                .isInstanceOf(BoardNotFoundException.class);
    }
    
    //==게시글 조회수 증가==//
    
    @Test
    @DisplayName("조회수 증가 성공")
    void increaseViewCount() {
        // given
        final long memberId = createUser("user@test.com", "user");
        final long boardId = createBoard(memberId, "title", "content", FREE);
        
        final Board board = boardRepository.findByIdAndStatus(boardId, BoardStatus.ACTIVE)
                                           .orElseThrow();
        
        assertThat(board.getViewCount()).isZero();
        
        // when
        boardService.increaseViewCount(boardId);
        
        // then
        assertThat(board.getViewCount()).isOne();
    }
    
    @Test
    @DisplayName("존재하지 않는 게시글은 조회수를 증가할 수 없다.")
    void increaseViewCountWithNotFoundBoard() {
        // when, then
        assertThatThrownBy(() -> boardService.increaseViewCount(Long.MAX_VALUE))
                .isInstanceOf(BoardNotFoundException.class);
    }
    
    @Test
    @DisplayName("삭제된 게시글은 조회수를 증가할 수 없다.")
    void increaseViewCountWithDeletedBoard() {
        // given
        final long memberId = createUser("user@test.com", "user");
        final long boardId = createBoard(memberId, "title", "content", FREE);
        
        final Board board = boardRepository.findByIdAndStatus(boardId, BoardStatus.ACTIVE)
                                           .orElseThrow();
        
        assertThat(board.getViewCount()).isZero();
        
        board.delete();
        assertThat(board.isDeleted()).isTrue();
        
        // when, then
        assertThatThrownBy(() -> boardService.increaseViewCount(boardId))
                .isInstanceOf(BoardNotFoundException.class);
        
        assertThat(board.getViewCount()).isZero();
    }
    
    //==게시글 페이징 조회==//
    
    @Test
    @DisplayName("게시글이 없으면 1페이지로 조회된다.")
    void getBoardPageWithWithoutBoards() {
        // given
        // 게시글 X
        
        // when
        final BoardPageResponse pageResponse = boardService.getBoardPage(1);
        
        // then
        assertThat(pageResponse.getTotalCount()).isEqualTo(0);
        assertThat(pageResponse.getBoardList()).isEmpty();
        
        assertThat(pageResponse.getTotalPages()).isEqualTo(1);
        assertThat(pageResponse.getCurrentPage()).isEqualTo(1);
        
        assertThat(pageResponse.getStartPage()).isEqualTo(1);
        assertThat(pageResponse.getEndPage()).isEqualTo(1);
        
        assertThat(pageResponse.hasPrevious()).isFalse();
        assertThat(pageResponse.hasNext()).isFalse();
    }
    
    @Test
    @DisplayName("첫 페이지 조회")
    void getFirstBoardPage() {
        // given
        final Long memberId = createUser("user@test.com", "user");
        
        final int BOARD_COUNT = 50;
        createBoards(memberId, BOARD_COUNT);
        
        // when
        final int REQUEST_PAGE = 1; // [1~15] | 16~30 | 31~45 | 46~50
        final BoardPageResponse pageResponse = boardService.getBoardPage(REQUEST_PAGE);
        
        // then
        assertThat(pageResponse.getTotalCount()).isEqualTo(BOARD_COUNT);
        
        assertThat(pageResponse.getBoardList()).hasSize(PAGE_SIZE);
        
        assertThat(pageResponse.getBoardList())
                .extracting(BoardListResponse::getBoardId)
                .isSortedAccordingTo(Comparator.reverseOrder()); // 최신순(내림차순) 검증
        
        assertThat(pageResponse.getTotalPages()).isEqualTo(4);
        
        assertThat(pageResponse.getCurrentPage()).isEqualTo(REQUEST_PAGE);
        
        assertThat(pageResponse.getStartPage()).isEqualTo(1);
        assertThat(pageResponse.getEndPage()).isEqualTo(4);
        
        assertThat(pageResponse.hasPrevious()).isFalse();
        assertThat(pageResponse.hasNext()).isTrue();
    }
    
    @Test
    @DisplayName("마지막 페이지 조회")
    void getLastBoardPage() {
        // given
        final Long memberId = createUser("user@test.com", "user");
        
        final int BOARD_COUNT = 50;
        createBoards(memberId, BOARD_COUNT);
        
        // when
        final int REQUEST_PAGE = 4; // 1~15 | 16~30 | 31~45 | [46~50]
        final BoardPageResponse pageResponse = boardService.getBoardPage(REQUEST_PAGE);
        
        // then
        assertThat(pageResponse.getTotalCount()).isEqualTo(BOARD_COUNT);
        
        assertThat(pageResponse.getBoardList()).hasSize(5);
        
        assertThat(pageResponse.getBoardList())
                .extracting(BoardListResponse::getBoardId)
                .isSortedAccordingTo(Comparator.reverseOrder()); // 최신순(내림차순) 검증
        
        assertThat(pageResponse.getTotalPages()).isEqualTo(4);
        assertThat(pageResponse.getCurrentPage()).isEqualTo(REQUEST_PAGE);
        
        assertThat(pageResponse.getStartPage()).isEqualTo(1);
        assertThat(pageResponse.getEndPage()).isEqualTo(4);
        
        assertThat(pageResponse.hasPrevious()).isTrue();
        assertThat(pageResponse.hasNext()).isFalse();
    }
    
    @Test
    @DisplayName("두 번째 페이지 블록 조회")
    void getSecondPageBlock() {
        // given
        final Long memberId = createUser("user@test.com", "user");
        final int BOARD_COUNT = 100;
        createBoards(memberId, BOARD_COUNT);
        
        //             block 1                    |      block 2
        //  (1)    (2)     (3)     (4)     (5)    |     (6)     (7)
        // 1~15 | 16~30 | 31~45 | 46~60 | 61~75   |   76~90 | 91~100
        
        // when - 1
        final int REQUEST_PAGE_1 = 6;
        final BoardPageResponse pageResponse1 = boardService.getBoardPage(REQUEST_PAGE_1);
        
        // then - 1
        assertThat(pageResponse1.getTotalCount()).isEqualTo(BOARD_COUNT);
        assertThat(pageResponse1.getBoardList()).hasSize(PAGE_SIZE);
        
        assertThat(pageResponse1.getTotalPages()).isEqualTo(7);
        assertThat(pageResponse1.getCurrentPage()).isEqualTo(REQUEST_PAGE_1);
        
        assertThat(pageResponse1.getStartPage()).isEqualTo(6);
        assertThat(pageResponse1.getEndPage()).isEqualTo(7);
        
        assertThat(pageResponse1.hasPrevious()).isTrue();
        assertThat(pageResponse1.hasNext()).isTrue();
        
        // when - 2
        final int REQUEST_PAGE_2 = 7;
        final BoardPageResponse pageResponse2 = boardService.getBoardPage(REQUEST_PAGE_2);
        
        // then - 2
        assertThat(pageResponse2.getTotalCount()).isEqualTo(BOARD_COUNT);
        
        assertThat(pageResponse2.getBoardList()).hasSize(10);
        
        assertThat(pageResponse2.getTotalPages()).isEqualTo(7);
        assertThat(pageResponse2.getCurrentPage()).isEqualTo(REQUEST_PAGE_2);
        
        assertThat(pageResponse2.getStartPage()).isEqualTo(6);
        assertThat(pageResponse2.getEndPage()).isEqualTo(7);
        
        assertThat(pageResponse2.hasPrevious()).isTrue();
        assertThat(pageResponse2.hasNext()).isFalse();
    }
    
    @Test
    @DisplayName("페이지 번호가 1보다 작으면 1페이지로 보정된다.")
    void clampPageToMinPage() {
        // given
        final Long memberId = createUser("user@test.com", "user");
        
        final int BOARD_COUNT = 50;
        createBoards(memberId, BOARD_COUNT);
        
        //  (1)    (2)     (3)     (4)
        // 1~15 | 16~30 | 31~45 | 46~50 |
        
        // when
        final int REQUEST_PAGE = -5;
        final BoardPageResponse pageResponse = boardService.getBoardPage(REQUEST_PAGE);
        
        // then
        assertThat(pageResponse.getTotalCount()).isEqualTo(BOARD_COUNT);
        
        assertThat(pageResponse.getBoardList()).hasSize(PAGE_SIZE);
        
        assertThat(pageResponse.getBoardList())
                .extracting(BoardListResponse::getBoardId)
                .isSortedAccordingTo(Comparator.reverseOrder());
        
        assertThat(pageResponse.getTotalPages()).isEqualTo(4);
        assertThat(pageResponse.getCurrentPage()).isEqualTo(1);
        
        assertThat(pageResponse.getStartPage()).isEqualTo(1);
        assertThat(pageResponse.getEndPage()).isEqualTo(4);
        
        assertThat(pageResponse.hasPrevious()).isFalse();
        assertThat(pageResponse.hasNext()).isTrue();
    }
    
    @Test
    @DisplayName("페이지 번호가 전체 페이지 수보다 크면 마지막 페이지로 보정된다.")
    void clampPageToLastPage() {
        // given
        final Long memberId = createUser("user@test.com", "user");
        
        final int BOARD_COUNT = 50;
        createBoards(memberId, BOARD_COUNT);
        
        //  (1)    (2)     (3)     (4)
        // 1~15 | 16~30 | 31~45 | 46~50 |
        
        // when
        final int REQUEST_PAGE = 7;
        final BoardPageResponse pageResponse = boardService.getBoardPage(REQUEST_PAGE);
        
        // then
        assertThat(pageResponse.getTotalCount()).isEqualTo(BOARD_COUNT);
        
        assertThat(pageResponse.getBoardList()).hasSize(5);
        
        assertThat(pageResponse.getBoardList())
                .extracting(BoardListResponse::getBoardId)
                .isSortedAccordingTo(Comparator.reverseOrder());
        
        assertThat(pageResponse.getTotalPages()).isEqualTo(4);
        assertThat(pageResponse.getCurrentPage()).isEqualTo(4);
        
        assertThat(pageResponse.getStartPage()).isEqualTo(1);
        assertThat(pageResponse.getEndPage()).isEqualTo(4);
        
        assertThat(pageResponse.hasPrevious()).isTrue();
        assertThat(pageResponse.hasNext()).isFalse();
    }
    
    //==게시글 수정 화면 조회==//
    
    @Test
    @DisplayName("작성자는 게시글 수정 화면을 조회할 수 있다.")
    void getBoardForUpdate() {
        // given
        final Long memberId = createUser("user@test.com", "user");
        final Long boardId = createBoard(memberId, "title", "content", FREE);
        
        // when
        final BoardUpdateResponse response = boardService.getBoardForUpdate(memberId, boardId);
        
        // then
        assertThat(response.getBoardId()).isEqualTo(boardId);
        assertThat(response.getTitle()).isEqualTo("title");
        assertThat(response.getContent()).isEqualTo("content");
        assertThat(response.getCategory()).isEqualTo(FREE);
    }
    
    @Test
    @DisplayName("작성자가 아닌 회원은, 게시글 수정 화면을 조회할 수 없다.")
    void getBoardForUpdateByNotWriter() {
        // given
        final Long writerId = createUser("writer@test.com", "writer");
        final Long boardId = createBoard(writerId, "title", "content", FREE);
        
        // when, then
        final Long notWriterId = createUser("notWriter@test.com", "notWriter");
        
        assertThatThrownBy(() -> boardService.getBoardForUpdate(notWriterId, boardId))
                .isInstanceOf(AccessDeniedException.class);
        
        assertThatThrownBy(() -> boardService.getBoardForUpdate(getAdminMemberId(), boardId))
                .isInstanceOf(AccessDeniedException.class);
    }
    
    @Test
    @DisplayName("삭제된 게시글은 수정 화면을 조회할 수 없다.")
    void getBoardForUpdateWithDeletedBoard() {
        // given
        final Long memberId = createUser("user@test.com", "user");
        final Long boardId = createBoard(memberId, "title", "content", FREE);
        
        final Board board = boardRepository.findById(boardId)
                                           .orElseThrow();
        board.delete(); // 게시글 삭제
        
        // when, then
        assertThatThrownBy(() -> boardService.getBoardForUpdate(memberId, boardId))
                .isInstanceOf(BoardNotFoundException.class);
    }
    
    @Test
    @DisplayName("존재하지 않는 게시글은 수정 화면을 조회할 수 없다.")
    void getBoardForUpdateWithNotFoundBoard() {
        // given
        final Long memberId = createUser("user@test.com", "user");
        
        // when, then
        assertThatThrownBy(() -> boardService.getBoardForUpdate(memberId, Long.MAX_VALUE))
                .isInstanceOf(BoardNotFoundException.class);
    }
    
    //==게시글 수정==//
    
    @Test
    @DisplayName("작성자는 게시글을 수정할 수 있다.")
    void updateBoardByWriter() {
        // given
        final Long memberId = createUser("user@test.com", "user");
        final Long boardId = createBoard(memberId, "title", "content", FREE);
        
        // when
        final String NEW_TITLE = "new title";
        final String NEW_CONTENT = "new content";
        final BoardCategory NEW_CATEGORY = JOB;
        
        final UpdateBoardRequest request = new UpdateBoardRequest(NEW_TITLE,
                                                                  NEW_CONTENT,
                                                                  NEW_CATEGORY);
        boardService.updateBoard(memberId, boardId, request);
        
        // then
        final Board board = boardRepository.findById(boardId)
                                           .orElseThrow();
        
        assertThat(board.getTitle()).isEqualTo(NEW_TITLE);
        assertThat(board.getContent()).isEqualTo(NEW_CONTENT);
        assertThat(board.getCategory()).isEqualTo(NEW_CATEGORY);
    }
    
    @Test
    @DisplayName("일반 회원은 공지사항으로 수정할 수 없다.")
    void updateNoticeByNormalMember() {
        // given
        final Long memberId = createUser("user@test.com", "user");
        final Long boardId = createBoard(memberId, "title", "content", FREE);
        
        // when, then
        final UpdateBoardRequest request = new UpdateBoardRequest("new title",
                                                                  "new content",
                                                                  NOTICE);
        
        assertThatThrownBy(() -> boardService.updateBoard(memberId, boardId, request))
                .isInstanceOf(AccessDeniedException.class);
    }
    
    @Test
    @DisplayName("관리자는 공지사항으로 수정할 수 있다.")
    void updateNoticeByAdminMember() {
        // given
        final Long adminMemberId = getAdminMemberId();
        
        final Long boardId = createBoard(adminMemberId, "title", "content", FREE);
        
        // when
        final String NEW_TITLE = "new title";
        final String NEW_CONTENT = "new content";
        final BoardCategory NEW_CATEGORY = NOTICE;
        
        final UpdateBoardRequest request = new UpdateBoardRequest(NEW_TITLE,
                                                                  NEW_CONTENT,
                                                                  NEW_CATEGORY);
        
        boardService.updateBoard(adminMemberId, boardId, request);
        
        // then
        final Board board = boardRepository.findById(boardId)
                                           .orElseThrow();
        
        assertThat(board.getTitle()).isEqualTo(NEW_TITLE);
        assertThat(board.getContent()).isEqualTo(NEW_CONTENT);
        assertThat(board.getCategory()).isEqualTo(NEW_CATEGORY);
    }
    
    @Test
    @DisplayName("작성자가 아닌 회원은, 게시글을 수정할 수 없다.")
    void updateBoardByNotWriter() {
        // given
        final Long writerId = createUser("writer@test.com", "writer");
        final Long boardId = createBoard(writerId, "title", "content", FREE);
        
        // when, then
        final Long notWriterId = createUser("notWriter@test.com", "notWriter");
        final UpdateBoardRequest request = new UpdateBoardRequest("new title",
                                                                  "new content",
                                                                  JOB);
        
        assertThatThrownBy(() -> boardService.updateBoard(notWriterId, boardId, request))
                .isInstanceOf(AccessDeniedException.class);
        
        assertThatThrownBy(() -> boardService.updateBoard(getAdminMemberId(), boardId, request))
                .isInstanceOf(AccessDeniedException.class);
    }
    
    @Test
    @DisplayName("삭제된 게시글은 수정할 수 없다.")
    void updateBoardWithDeletedBoard() {
        // given
        final Long memberId = createUser("user@test.com", "user");
        final Long boardId = createBoard(memberId, "title", "content", FREE);
        
        final Board board = boardRepository.findById(boardId)
                                           .orElseThrow();
        board.delete(); // 게시글 삭제
        
        // when, then
        final UpdateBoardRequest request = new UpdateBoardRequest("new title",
                                                                  "new content",
                                                                  QNA);
        
        assertThatThrownBy(() -> boardService.updateBoard(memberId, boardId, request))
                .isInstanceOf(BoardNotFoundException.class);
    }
    
    @Test
    @DisplayName("존재하지 않는 게시글은 수정할 수 없다.")
    void updateBoardWithNotFoundBoard() {
        // given
        final Long memberId = createUser("user@test.com", "user");
        
        // when, then
        final UpdateBoardRequest request = new UpdateBoardRequest("new title",
                                                                  "new content",
                                                                  QNA);
        
        assertThatThrownBy(() -> boardService.updateBoard(memberId, Long.MAX_VALUE, request))
                .isInstanceOf(BoardNotFoundException.class);
    }
    
    //==게시글 삭제==//
    
    @Test
    @DisplayName("작성자는 게시글을 삭제할 수 있다.")
    void deleteBoardByWriter() {
        // given
        final long memberId = createUser("user@test.com", "tester");
        final long boardId = createBoard(memberId, "title", "content", FREE);
        
        // when
        boardService.deleteBoard(memberId, boardId);
        
        // then
        final Board board = boardRepository.findById(boardId)
                                           .orElseThrow();
        
        assertThat(board.isDeleted()).isTrue();
    }
    
    @Test
    @DisplayName("관리자는 다른 회원의 게시글을 삭제할 수 있다.")
    void deleteBoardByAdminMember() {
        // given
        final long memberId = createUser("user@test.com", "tester");
        final long boardId = createBoard(memberId, "title", "content", FREE);
        
        // when
        boardService.deleteBoard(getAdminMemberId(),
                                 boardId);
        
        // then
        final Board board = boardRepository.findById(boardId)
                                           .orElseThrow();
        assertThat(board.isDeleted()).isTrue();
    }
    
    @Test
    @DisplayName("작성자가 아닌 회원은 게시글을 삭제할 수 없다.")
    void deleteBoardByNotWriter() {
        // given
        final long writerId = createUser("writer@test.com", "writer");
        final long boardId = createBoard(writerId, "title", "content", FREE);
        
        // when, then
        final long notWriterId = createUser("notWriter@test.com", "notWriter");
        
        assertThatThrownBy(() -> boardService.deleteBoard(notWriterId, boardId))
                .isInstanceOf(AccessDeniedException.class);
    }
    
    @Test
    @DisplayName("삭제된 게시글은 다시 삭제할 수 없다.")
    void deleteBoardWithDeletedBoard() {
        // given
        final long memberId = createUser("user@test.com", "tester");
        final long boardId = createBoard(memberId, "title", "content", FREE);
        
        boardService.deleteBoard(memberId, boardId); // 게시글 삭제
        
        final Board board = boardRepository.findById(boardId)
                                           .orElseThrow();
        assertThat(board.isDeleted()).isTrue();
        
        // when, then
        // 삭제 게시글 재삭제
        assertThatThrownBy(() -> boardService.deleteBoard(memberId, boardId))
                .isInstanceOf(BoardNotFoundException.class);
    }
    
    @Test
    @DisplayName("존재하지 않는 게시글은 삭제할 수 없다.")
    void deleteBoardWithNotFoundBoard() {
        // given
        final long memberId = createUser("user@test.com", "tester");
        
        // when, then
        assertThatThrownBy(() -> boardService.deleteBoard(memberId, Long.MAX_VALUE))
                .isInstanceOf(BoardNotFoundException.class);
    }
    
    @Test
    @DisplayName("탈퇴한 회원은 게시글을 삭제할 수 없다.")
    void deleteBoardByWithdrawnMember() {
        // given
        final long memberId = createUser("user@test.com", "tester");
        final long boardId = createBoard(memberId, "title", "content", FREE);
        
        memberService.withdraw(memberId); // 회원 탈퇴
        
        final Member member = memberRepository.findById(memberId)
                                              .orElseThrow();
        assertThat(member.getStatus())
                .isEqualTo(MemberStatus.DELETED);
        
        // when, then
        assertThatThrownBy(() -> boardService.deleteBoard(memberId, boardId))
                .isInstanceOf(MemberNotFoundException.class);
    }
    
    //==편의 메서드==//
    
    private Long createUser(final String email, final String nickname) {
        return memberService.signup(new SignupRequest(email,
                                                      "password123",
                                                      nickname,
                                                      Gender.NONE));
    }
    
    private Long getAdminMemberId() {
        return memberRepository.findByEmail(ADMIN_EMAIL)
                               .orElseThrow()
                               .getId();
    }
    
    private Long createBoard(final Long memberId,
                             final String title,
                             final String content,
                             final BoardCategory category) {
        assert (memberId != null);
        assert (category != null);
        
        return boardService.writeBoard(memberId,
                                       new WriteBoardRequest(title,
                                                             content,
                                                             category));
    }
    
    private void createBoards(final Long memberId,
                              final int count) {
        assert (memberId != null);
        
        for (int i = 0; i < count; ++i) {
            createBoard(memberId,
                        "title" + i,
                        "content" + i,
                        FREE);
        }
    }
}

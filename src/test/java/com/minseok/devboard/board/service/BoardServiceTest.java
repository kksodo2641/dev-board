package com.minseok.devboard.board.service;

import com.minseok.devboard.board.dto.request.WriteBoardRequest;
import com.minseok.devboard.board.dto.response.BoardDetailResponse;
import com.minseok.devboard.board.entity.Board;
import com.minseok.devboard.board.entity.BoardCategory;
import com.minseok.devboard.board.entity.BoardStatus;
import com.minseok.devboard.board.exception.BoardNotFoundException;
import com.minseok.devboard.board.repository.BoardRepository;
import com.minseok.devboard.global.exception.AccessDeniedException;
import com.minseok.devboard.member.dto.request.SignupRequest;
import com.minseok.devboard.member.entity.Gender;
import com.minseok.devboard.member.exception.MemberNotFoundException;
import com.minseok.devboard.member.repository.MemberRepository;
import com.minseok.devboard.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
class BoardServiceTest {
    
    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    
    @Autowired BoardService boardService;
    @Autowired BoardRepository boardRepository;
    
    //==게시글 작성==//
    @Test
    @DisplayName("ACTIVE 회원은 일반 게시글을 작성할 수 있다.")
    void writeBoardWithActiveMember() {
        // given
        final Long memberId = createUser("writeBoardWithActiveMember@test.com",
                                         "writeBoardWithActiveMember");
        
        final WriteBoardRequest writeBoardRequest = new WriteBoardRequest("title",
                                                                          "content",
                                                                          BoardCategory.FREE);
        
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
    void writeBoardWithNotFoundMember() {
        // given
        final WriteBoardRequest writeBoardRequest = new WriteBoardRequest("title",
                                                                          "content",
                                                                          BoardCategory.FREE);
        
        // when, then
        assertThatThrownBy(() -> boardService.writeBoard(Long.MAX_VALUE, writeBoardRequest))
                .isInstanceOf(MemberNotFoundException.class);
    }
    
    @Test
    @DisplayName("탈퇴한 회원은 게시글을 작성할 수 없다.")
    void writeBoardWithWithdrawalMember() {
        // given
        final Long memberId = createUser("writeBoardWithWithdrawalMember@test.com",
                                         "writeBoardWithWithdrawalMember");
        
        final WriteBoardRequest writeBoardRequest = new WriteBoardRequest("title",
                                                                          "content",
                                                                          BoardCategory.FREE);
        
        memberService.withdraw(memberId);
        
        // when, then
        assertThatThrownBy(() -> boardService.writeBoard(memberId, writeBoardRequest))
                .isInstanceOf(MemberNotFoundException.class);
    }
    
    @Test
    @DisplayName("일반 회원은 공지사항을 작성할 수 없다.")
    void writeNoticeWithNormalMember() {
        // given
        final Long memberId = createUser("writeNoticeWithNormalMember@test.com",
                                         "writeNoticeWithNormalMember");
        
        final WriteBoardRequest writeBoardRequest = new WriteBoardRequest("title",
                                                                          "content",
                                                                          BoardCategory.NOTICE);
        
        // when, then
        assertThatThrownBy(() -> boardService.writeBoard(memberId, writeBoardRequest))
                .isInstanceOf(AccessDeniedException.class);
    }
    
    @Test
    @DisplayName("관리자는 공지사항을 작성할 수 있다.")
    void writeNoticeWithAdminMember() {
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
    void readBoardSuccess() {
        // given
        final Long memberId = createUser("readBoardSuccess@test.com",
                                         "readBoardSuccess");
        
        final Long boardId = createBoard(memberId, BoardCategory.QNA);
        
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
    void readBoardFailByNotFoundBoard() {
        // when, then
        assertThatThrownBy(() -> boardService.readBoard(Long.MAX_VALUE))
                .isInstanceOf(BoardNotFoundException.class);
    }
    
    @Test
    @DisplayName("게시글 조회 실패 - 삭제된 게시글")
    void readBoardFailByDeletedBoard() {
        // given
        final Long memberId = createUser("readBoardFailByDeletedBoard@test.com",
                                         "readBoardFailByDeletedBoard");
        
        final Long boardId = createBoard(memberId, BoardCategory.JOB);
        
        // 게시글 삭제
        final Board board = boardRepository.findById(boardId)
                                           .orElseThrow();
        board.delete();
        
        assertThat(board.getStatus()).isEqualTo(BoardStatus.DELETED);
        
        // when, then
        assertThatThrownBy(() -> boardService.readBoard(boardId))
                .isInstanceOf(BoardNotFoundException.class);
    }
    
    //==편의 메서드==//
    private Long createUser(final String email, final String nickname) {
        return memberService.signup(new SignupRequest(email,
                                                      "password123",
                                                      nickname,
                                                      Gender.NONE));
    }
    
    private Long getAdminMemberId() {
        final String ADMIN_EMAIL = "admin@devboard.com";
        return memberRepository.findByEmail(ADMIN_EMAIL)
                               .orElseThrow()
                               .getId();
    }
    
    private Long createBoard(final Long memberId, final BoardCategory category) {
        assert (memberId != null);
        assert (category != null);
        
        return boardService.writeBoard(memberId,
                                       new WriteBoardRequest("title",
                                                             "content",
                                                             category));
    }
}
















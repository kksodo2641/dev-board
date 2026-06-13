package com.minseok.devboard.board.service;

import com.minseok.devboard.board.dto.request.WriteBoardRequest;
import com.minseok.devboard.board.entity.Board;
import com.minseok.devboard.board.entity.BoardCategory;
import com.minseok.devboard.board.entity.BoardStatus;
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
    
    private static final String ADMIN_EMAIL = "admin@devboard.com";
    
    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    
    @Autowired BoardService boardService;
    @Autowired BoardRepository boardRepository;
    
    //==게시글 작성==//
    @Test
    @DisplayName("ACTIVE 회원은 일반 게시글을 작성할 수 있다.")
    void writeBoardWithActiveMember() {
        // given
        final Long memberId = createNormalMember("writeBoardWithActiveMember@example.com",
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
        final Long memberId = createNormalMember("writeBoardWithWithdrawalMember@example.com",
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
        final Long memberId = createNormalMember("writeNoticeWithNormalMember@example.com",
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
        final Long memberId = memberRepository.findByEmail(ADMIN_EMAIL)
                                              .orElseThrow()
                                              .getId();
        
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
    
    private Long createNormalMember(final String email, final String nickname) {
        return memberService.signup(new SignupRequest(email,
                                                      "password123",
                                                      nickname,
                                                      Gender.NONE));
    }
}
















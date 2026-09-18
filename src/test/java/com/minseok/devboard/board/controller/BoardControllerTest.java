package com.minseok.devboard.board.controller;

import com.minseok.devboard.IntegrationTest;
import com.minseok.devboard.board.dto.request.WriteBoardRequest;
import com.minseok.devboard.board.dto.response.BoardDetailResponse;
import com.minseok.devboard.board.entity.BoardCategory;
import com.minseok.devboard.board.service.BoardService;
import com.minseok.devboard.member.dto.request.SignupRequest;
import com.minseok.devboard.member.entity.Gender;
import com.minseok.devboard.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@AutoConfigureMockMvc
class BoardControllerTest extends IntegrationTest {
    
    private static final String VIEWED_BOARDS_COOKIE = "viewedBoards";
    
    @Autowired MemberService memberService;
    @Autowired BoardService boardService;
    @Autowired MockMvc mockMvc;
    
    @Test
    @DisplayName("처음 조회한 게시글은 조회수를 증가시키고 조회 기록 Cookie를 추가한다.")
    void increaseViewCountAndAddViewedBoardCookieOnFirstView() throws Exception {
        // given
        final Long memberId = memberService.signup(
                new SignupRequest("test@example.com",
                                  "testPassword",
                                  "testNickname",
                                  Gender.NONE));
        
        final Long boardId = boardService.writeBoard(
                memberId,
                new WriteBoardRequest("title",
                                      "content",
                                      BoardCategory.FREE));
        
        // when & then
        mockMvc.perform(get("/boards/{boardId}", boardId))
               .andExpect(status().isOk())
               .andExpect(view().name("board/detail"))
               .andExpect(cookie().value(VIEWED_BOARDS_COOKIE,
                                         String.valueOf(boardId)))
               .andExpect(cookie().maxAge(VIEWED_BOARDS_COOKIE,
                                          60 * 60 * 24))
               .andExpect(cookie().path(VIEWED_BOARDS_COOKIE,
                                        "/boards"));
        
        final BoardDetailResponse response = boardService.readBoard(boardId);
        assertThat(response.getViewCount()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("존재하지 않는 게시글의 조회 기록 Cookie는 추가하지 않는다.")
    void doesNotAddViewedBoardCookieForNotFoundBoard() throws Exception {
        // given
        final Long boardId = Long.MAX_VALUE;
        
        // when & then
        mockMvc.perform(get("/boards/{boardId}", boardId))
               .andExpect(status().isNotFound())
               .andExpect(view().name("error/404"))
               .andExpect(model().attribute("userMessage",
                                            "요청한 게시글을 찾을 수 없습니다."))
               .andExpect(cookie().doesNotExist(VIEWED_BOARDS_COOKIE));
    }
    
    @Test
    @DisplayName("삭제된 게시글의 조회 기록 Cookie는 추가하지 않는다.")
    void doesNotAddViewedBoardCookieForDeletedBoard() throws Exception {
        // given
        final Long memberId = memberService.signup(
                new SignupRequest("test@example.com",
                                  "testPassword",
                                  "testNickname",
                                  Gender.NONE));
        
        final Long boardId = boardService.writeBoard(
                memberId,
                new WriteBoardRequest("title",
                                      "content",
                                      BoardCategory.FREE));
        
        boardService.deleteBoard(memberId, boardId);
        
        // when & then
        mockMvc.perform(get("/boards/{boardId}", boardId))
               .andExpect(status().isNotFound())
               .andExpect(view().name("error/404"))
               .andExpect(model().attribute("userMessage",
                                            "요청한 게시글을 찾을 수 없습니다."))
               .andExpect(cookie().doesNotExist(VIEWED_BOARDS_COOKIE));
    }
}

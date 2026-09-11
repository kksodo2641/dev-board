package com.minseok.devboard.global.exception;

import com.minseok.devboard.HomeController;
import com.minseok.devboard.board.exception.BoardNotFoundException;
import com.minseok.devboard.member.exception.MemberNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static com.minseok.devboard.global.common.SessionConst.LOGIN_MEMBER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class SsrExceptionHandlerTest {
    
    private MockMvc mockMvc;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                                 .setControllerAdvice(new SsrExceptionHandler())
                                 .build();
    }
    
    @Test
    @DisplayName("요청 값의 타입이 일치하지 않으면 400 Bad Request 오류 화면을 렌더링한다.")
    void renderBadRequestViewForTypeMismatch() throws Exception {
        // given
        final String requestURI = "/test/type-mismatch/abc";
        
        // when & then
        final MvcResult mvcResult = mockMvc.perform(get(requestURI))
                                           .andExpect(status().isBadRequest())
                                           .andExpect(view().name("error/400"))
                                           .andExpect(model().attribute("userMessage",
                                                                        "요청 값의 형식이 올바르지 않습니다."))
                                           .andReturn();
        
        assertThat(mvcResult.getResolvedException())
                .isInstanceOf(MethodArgumentTypeMismatchException.class);
    }
    
    @Test
    @DisplayName("접근 권한이 없으면 403 Forbidden 오류 화면을 렌더링한다.")
    void renderForbiddenViewForAccessDenied() throws Exception {
        // given
        final String requestURI = "/test/access-denied";
        
        // when & then
        final MvcResult mvcResult = mockMvc.perform(get(requestURI))
                                           .andExpect(status().isForbidden())
                                           .andExpect(view().name("error/403"))
                                           .andExpect(model().attribute("userMessage",
                                                                        "요청을 수행할 권한이 없습니다."))
                                           .andReturn();
        
        assertThat(mvcResult.getResolvedException())
                .isInstanceOf(AccessDeniedException.class);
    }
    
    @Test
    @DisplayName("로그인 회원을 찾을 수 없으면 세션을 무효화하고 로그인 페이지로 리다이렉트한다.")
    void invalidateSessionAndRedirectToLoginForMemberNotFound() throws Exception {
        // given
        final String requestURI = "/test/member-not-found";
        
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute(LOGIN_MEMBER_ID, 1L);
        
        // when & then
        final MvcResult mvcResult = mockMvc.perform(get(requestURI)
                                                            .session(session))
                                           .andExpect(status().isFound())
                                           .andExpect(redirectedUrl("/members/login?sessionInvalidated=true"))
                                           .andReturn();
        
        assertThat(session.isInvalid()).isTrue();
        
        assertThat(mvcResult.getResolvedException())
                .isInstanceOf(MemberNotFoundException.class);
    }
    
    @Test
    @DisplayName("게시글을 찾을 수 없으면 404 Not Found 오류 화면을 렌더링한다.")
    void renderNotFoundViewForBoardNotFound() throws Exception {
        // given
        final String requestURI = "/test/board-not-found";
        
        // when & then
        final MvcResult mvcResult = mockMvc.perform(get(requestURI))
                                           .andExpect(status().isNotFound())
                                           .andExpect(view().name("error/404"))
                                           .andExpect(model().attribute("userMessage",
                                                                        "요청한 게시글을 찾을 수 없습니다."))
                                           .andReturn();
        
        assertThat(mvcResult.getResolvedException())
                .isInstanceOf(BoardNotFoundException.class);
    }
    
    @Controller
    @RequestMapping("/test")
    static class TestController extends HomeController {
        
        @GetMapping("/type-mismatch/{num}")
        public void typeMismatch(final @PathVariable("num") int num) {
        }
        
        @GetMapping("/access-denied")
        public void throwAccessDenied() {
            throw new AccessDeniedException();
        }
        
        @GetMapping("/member-not-found")
        public void throwMemberNotFound() {
            throw new MemberNotFoundException();
        }
        
        @GetMapping("/board-not-found")
        public void throwBoardNotFound() {
            throw new BoardNotFoundException();
        }
    }
}

package com.minseok.devboard.member.controller;

import com.minseok.devboard.IntegrationTest;
import com.minseok.devboard.member.dto.request.SignupRequest;
import com.minseok.devboard.member.entity.Gender;
import com.minseok.devboard.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static com.minseok.devboard.global.common.SessionConst.LOGIN_MEMBER_ID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class MemberControllerTest extends IntegrationTest {
    
    private static final String LOGIN_URL = "/members/login";
    private static final String DEFAULT_REDIRECT_URL = "/";
    
    private static final String EMAIL = "redirectTest@test.com";
    private static final String PASSWORD = "redirectTestPassword";
    
    private @Autowired MockMvc mockMvc;
    private @Autowired MemberService memberService;
    
    private Long memberId;
    
    @BeforeEach
    void setUp() {
        memberId = memberService.signup(new SignupRequest(EMAIL,
                                                          PASSWORD,
                                                          "redirectTestNickname",
                                                          Gender.NONE));
    }
    
    @Test
    @DisplayName("로그인 후 이동 경로가 없으면 기본 경로로 이동한다.")
    void redirectToDefaultURLWithoutRedirectURL() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                                .param("email", EMAIL)
                                .param("password", PASSWORD))
               .andExpect(status().isFound())
               .andExpect(redirectedUrl(DEFAULT_REDIRECT_URL))
               .andExpect(request().sessionAttribute(LOGIN_MEMBER_ID, memberId));
    }
    
    @ParameterizedTest(name = "[{index}] redirectURL={0}")
    @ValueSource(strings = {
            "/boards",
            "/boards?page=2",
            "/boards?keyword=https://example.com",
            "/unknown"
    })
    @DisplayName("로그인 후 유효한 애플리케이션 내부 경로로 이동한다.")
    void redirectToValidInternalURL(final String redirectURL) throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                                .param("redirectURL", redirectURL)
                                .param("email", EMAIL)
                                .param("password", PASSWORD))
               .andExpect(status().isFound())
               .andExpect(redirectedUrl(redirectURL))
               .andExpect(request().sessionAttribute(LOGIN_MEMBER_ID, memberId));
    }
    
    @ParameterizedTest(name = "[{index}] redirectURL={0}")
    @ValueSource(strings = {
            "https://example.com",
            "//example.com"
    })
    @DisplayName("로그인 후 외부 URL 대신 기본 경로로 이동한다.")
    void replaceExternalRedirectURLWithDefaultURL(final String redirectURL) throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                                .param("redirectURL", redirectURL)
                                .param("email", EMAIL)
                                .param("password", PASSWORD))
               .andExpect(status().isFound())
               .andExpect(redirectedUrl(DEFAULT_REDIRECT_URL))
               .andExpect(request().sessionAttribute(LOGIN_MEMBER_ID, memberId));
    }
    
    @ParameterizedTest(name = "[{index}] redirectURL={0}")
    @ValueSource(strings = {
            "boards",
            "///example.com",
            "/%2Fexample.com", // "//example.com"
            "/%5Cexample.com", // "/\example.com"
            "/boards#comments",
            "/boards[",
    })
    @DisplayName("로그인 후 유효하지 않은 이동 경로 대신 기본 경로로 이동한다.")
    void replaceInvalidRedirectURLWithDefaultURL(final String redirectURL) throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                                .param("redirectURL", redirectURL)
                                .param("email", EMAIL)
                                .param("password", PASSWORD))
               .andExpect(status().isFound())
               .andExpect(redirectedUrl(DEFAULT_REDIRECT_URL))
               .andExpect(request().sessionAttribute(LOGIN_MEMBER_ID, memberId));
    }
}

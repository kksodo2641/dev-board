package com.minseok.devboard.global.interceptor;

import com.minseok.devboard.global.exception.api.ApiErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static com.minseok.devboard.global.common.SessionConst.LOGIN_MEMBER_ID;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class LoginCheckInterceptorTest {
    
    private JsonMapper jsonMapper;
    private LoginCheckInterceptor interceptor;
    
    @BeforeEach
    void setUp() {
        jsonMapper = JsonMapper.builder().build();
        interceptor = new LoginCheckInterceptor(jsonMapper);
    }
    
    @Test
    @DisplayName("@PublicAccess가 붙은 요청은 비로그인 사용자에게도 허용한다.")
    void allowPublicAccessRequestWithoutLogin() throws Exception {
        // given
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/public");
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final HandlerMethod handler = new HandlerMethod(new TestViewController(),
                                                        "publicAccess");
        
        // when
        final boolean result = interceptor.preHandle(request, response, handler);
        
        // then
        assertThat(result).isTrue();
    }
    
    @Test
    @DisplayName("정적 리소스 요청은 로그인 여부와 관계없이 허용한다.")
    void allowStaticResourceRequestWithoutLogin() throws Exception {
        // given
        final String requestURI = "/static-resource";
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", requestURI);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final ResourceHttpRequestHandler handler = new ResourceHttpRequestHandler();
        
        // when
        final boolean result = interceptor.preHandle(request, response, handler);
        
        // then
        assertThat(result).isTrue();
    }
    
    @Test
    @DisplayName("알 수 없는 Handler의 비로그인 요청은 로그인 페이지로 리다이렉트한다.")
    void redirectUnknownHandlerRequestWithoutLogin() throws Exception {
        // given
        final String requestURI = "/unknown-handler";
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", requestURI);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final Object handler = new Object();
        
        // when
        final boolean result = interceptor.preHandle(request, response, handler);
        
        // then
        assertThat(result).isFalse();
        
        assertThat(response.getStatus())
                .isEqualTo(HttpStatus.FOUND.value());
        
        assertThat(response.getRedirectedUrl())
                .isEqualTo("/members/login?redirectURL=" + encode(requestURI, UTF_8));
    }
    
    @Test
    @DisplayName("세션이 존재해도 로그인 회원 ID가 없으면 미인증 요청으로 처리한다.")
    void rejectProtectedRequestWithoutLoginMemberIdInSession() throws Exception {
        // given
        final String requestURI = "/members/me";
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", requestURI);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final HandlerMethod handler = new HandlerMethod(new TestViewController(),
                                                        "ssr");
        
        request.getSession(true);
        
        // when
        final boolean result = interceptor.preHandle(request, response, handler);
        
        // then
        assertThat(result).isFalse();
    }
    
    @ParameterizedTest
    @DisplayName("로그인 사용자의 보호된 요청은 허용한다.")
    @CsvSource({
            "GET, /members/me",           // 마이페이지
            "GET, /boards/write",         // 게시글 작성 폼
            "POST, /boards/1/comments",   // 댓글 작성
            "PATCH, /comments/1",         // 댓글 수정
            "DELETE, /comments/1",        // 댓글 삭제
    })
    void allowProtectedRequestWithLogin(final String httpMethod,
                                        final String requestURI) throws Exception {
        // given
        final MockHttpServletRequest request = new MockHttpServletRequest(httpMethod, requestURI);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        
        final HandlerMethod handler = new HandlerMethod(new TestViewController(), "ssr");
        
        final HttpSession session = request.getSession(true);
        session.setAttribute(LOGIN_MEMBER_ID, 1L);
        
        // when
        final boolean result = interceptor.preHandle(request, response, handler);
        
        // then
        assertThat(result).isTrue();
    }
    
    @Test
    @DisplayName("비로그인 사용자의 보호된 SSR 요청은 로그인 페이지로 리다이렉트한다.")
    void redirectProtectedSsrRequestWithoutLogin() throws Exception {
        // given
        final String requestURI = "/boards/write";
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", requestURI);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final HandlerMethod handler = new HandlerMethod(new TestViewController(), "ssr");
        
        // when
        final boolean result = interceptor.preHandle(request, response, handler);
        
        // then
        assertThat(result).isFalse();
        
        assertThat(response.getStatus())
                .isEqualTo(HttpServletResponse.SC_FOUND);
        
        assertThat(response.getRedirectedUrl())
                .isEqualTo("/members/login?redirectURL=" + encode(requestURI, UTF_8));
    }
    
    @Test
    @DisplayName("리다이렉트 시 요청 URI와 쿼리 스트링을 보존한다.")
    void preserveRequestURIAndQueryStringWhenRedirect() throws Exception {
        // given
        final String requestURI = "/members/me";
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", requestURI);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final HandlerMethod handler = new HandlerMethod(new TestViewController(), "ssr");
        
        final String queryString = "a=hello&b=20&c=ab34cd";
        request.setQueryString(queryString);
        
        // when
        final boolean result = interceptor.preHandle(request, response, handler);
        
        // then
        assertThat(result).isFalse();
        
        assertThat(response.getStatus())
                .isEqualTo(HttpServletResponse.SC_FOUND);
        
        assertThat(response.getRedirectedUrl())
                .isEqualTo("/members/login?redirectURL="
                                   + encode(requestURI + "?" + queryString, UTF_8));
    }
    
    @ParameterizedTest
    @DisplayName("비로그인 사용자의 API 요청은 LOGIN_REQUIRED JSON과 401 Unauthorized를 응답한다.")
    @MethodSource("apiRequestCases")
    void rejectApiRequestWithoutLogin(final String httpMethod,
                                      final String requestURI,
                                      final Class<?> controllerClass,
                                      final String methodName) throws Exception {
        // given
        final MockHttpServletRequest request = new MockHttpServletRequest(httpMethod, requestURI);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        
        final Object controller = controllerClass.getDeclaredConstructor()
                                                 .newInstance();
        final HandlerMethod handler = new HandlerMethod(controller, methodName);
        
        // when
        final boolean result = interceptor.preHandle(request, response, handler);
        
        // then
        final int status = response.getStatus();
        final MediaType mediaType = MediaType.parseMediaType(response.getContentType());
        final String characterEncoding = response.getCharacterEncoding();
        final JsonNode responseBody = jsonMapper.readTree(response.getContentAsString());
        
        assertThat(result).isFalse();
        
        assertThat(status)
                .isEqualTo(HttpStatus.UNAUTHORIZED.value());
        
        assertThat(mediaType.isCompatibleWith(MediaType.APPLICATION_JSON))
                .isTrue();
        
        assertThat(characterEncoding)
                .isEqualTo(StandardCharsets.UTF_8.name());
        
        assertThat(responseBody.get("code").asString())
                .isEqualTo(ApiErrorCode.LOGIN_REQUIRED.getCode());
        
        assertThat(responseBody.get("message").asString())
                .isEqualTo(ApiErrorCode.LOGIN_REQUIRED.getMessage());
    }
    
    private static Stream<Arguments> apiRequestCases() {
        return Stream.of(
                Arguments.of(
                        "POST",
                        "/boards/1/comments",
                        TestRestController.class,
                        "api"
                ),
                Arguments.of(
                        "PATCH",
                        "/comments/1",
                        TestResponseBodyController.class,
                        "api"
                ),
                Arguments.of(
                        "DELETE",
                        "/comments/1",
                        TestResponseBodyController.class,
                        "api"
                )
        );
    }
    
    @Controller
    static class TestViewController {
        
        public void ssr() {
        }
        
        @PublicAccess
        public void publicAccess() {
        }
    }
    
    @RestController
    static class TestRestController {
        
        public void api() {
        }
    }
    
    @Controller
    static class TestResponseBodyController {
        
        @ResponseBody
        public void api() {
        }
    }
}

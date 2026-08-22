package com.minseok.devboard.global.interceptor;

import com.minseok.devboard.global.exception.api.ApiErrorCode;
import com.minseok.devboard.global.exception.api.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.minseok.devboard.global.common.SessionConst.LOGIN_MEMBER_ID;
import static org.springframework.core.annotation.AnnotatedElementUtils.hasAnnotation;

@Component
@RequiredArgsConstructor
public class LoginCheckInterceptor implements HandlerInterceptor {
    
    private final JsonMapper jsonMapper;
    
    @Override
    public boolean preHandle(final HttpServletRequest request,
                             final HttpServletResponse response,
                             final Object handler) throws IOException {
        // 공개 접근 경로: 게시글 목록, 게시글 상세, 댓글 목록
        if (isPublicRequest(request)) {
            return true;
        }
        
        final HttpSession session = request.getSession(false);
        if (session != null
                && session.getAttribute(LOGIN_MEMBER_ID) != null) {
            return true;
        }
        
        // 미인증 or 세션이 만료된 경우
        
        if (isApiRequest(handler)) {
            // API 요청 -> 401 Unauthorized + LOGIN_REQUIRED JSON
            writeLoginRequiredResponse(response);
            
        } else {
            // SSR 요청 -> 302 Found (로그인 페이지로 redirect)
            response.sendRedirect("/members/login?redirectURL="
                                          + getEncodedRedirectURL(request));
        }
        
        return false;
    }
    
    private void writeLoginRequiredResponse(
            final HttpServletResponse response) throws IOException {
        
        assert (response != null);
        
        final ApiErrorCode errorCode = ApiErrorCode.LOGIN_REQUIRED;
        
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8);
        
        jsonMapper.writeValue(response.getWriter(),
                              ApiErrorResponse.from(errorCode));
    }
    
    /**
     * 비로그인 시에도 접근 가능한 요청인지 확인
     * <p>
     * 1. 게시글 목록
     * <p>
     * 2. 게시글 상세
     * <p>
     * 3. 댓글 목록
     */
    private static boolean isPublicRequest(final HttpServletRequest request) {
        assert (request != null);
        
        final String requestMethod = request.getMethod();
        final String requestURI = request.getRequestURI();
        
        final boolean isGet = requestMethod.equals("GET");
        
        final boolean isBoardList = requestURI.equals("/boards");
        final boolean isBoardDetail = requestURI.matches("^/boards/\\d+$"); // 예: /boards/123
        final boolean isCommentList = requestURI.matches("^/boards/\\d+/comments$");
        
        return isGet && (isBoardList || isBoardDetail || isCommentList);
    }
    
    private static boolean isApiRequest(final Object handler) {
        assert (handler != null);
        
        // Controller 메서드가 처리하는 요청만 고려
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return false;
        }
        
        final boolean hasResponseBodyOnClass = hasAnnotation(handlerMethod.getBeanType(),
                                                             ResponseBody.class);
        
        final boolean hasResponseBodyOnMethod = hasAnnotation(handlerMethod.getMethod(),
                                                              ResponseBody.class);
        
        return hasResponseBodyOnClass || hasResponseBodyOnMethod;
    }
    
    private static String getEncodedRedirectURL(final HttpServletRequest request) {
        assert (request != null);
        
        final String requestURI = request.getRequestURI();
        final String queryString = request.getQueryString();
        
        final String redirectURL = (queryString == null)
                                   ? requestURI
                                   : requestURI + "?" + queryString;
        
        return URLEncoder.encode(redirectURL, StandardCharsets.UTF_8);
    }
}

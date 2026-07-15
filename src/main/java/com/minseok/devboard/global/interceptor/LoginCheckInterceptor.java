package com.minseok.devboard.global.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.minseok.devboard.global.common.SessionConst.LOGIN_MEMBER_ID;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(final HttpServletRequest request,
                             final HttpServletResponse response,
                             final Object handler) throws Exception {
        // 비로그인 시에도 게시글 목록/상세, 댓글 목록 조회 허용
        if (isPublicRequest(request)) {
            return true;
        }
        
        final HttpSession session = request.getSession(false);
        if (session != null
                && session.getAttribute(LOGIN_MEMBER_ID) != null) {
            return true;
        }
        
        // 비로그인 시, 로그인 화면으로 이동
        response.sendRedirect("/members/login?redirectURL="
                                      + getEncodedRedirectURL(request));
        return false;
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

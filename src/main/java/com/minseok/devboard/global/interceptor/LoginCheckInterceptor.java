package com.minseok.devboard.global.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.minseok.devboard.global.common.SessionConst.LOGIN_MEMBER_ID;

@Component
@RequiredArgsConstructor
public class LoginCheckInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(final HttpServletRequest request,
                             final HttpServletResponse response,
                             final Object handler) throws Exception {
        // 비로그인 시에도, 게시글 목록/상세 조회 허용
        if (isPublicBoardRequest(request.getRequestURI())) {
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
     * [게시글 목록/상세 조회] -> 비로그인도 접근 허용
     */
    private static boolean isPublicBoardRequest(final String requestURI) {
        assert (requestURI != null);
        
        final boolean isBoardList = requestURI.equals("/boards");
        final boolean isBoardDetail = requestURI.matches("^/boards/\\d+$"); // 예: /boards/123
        
        return isBoardList || isBoardDetail;
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


















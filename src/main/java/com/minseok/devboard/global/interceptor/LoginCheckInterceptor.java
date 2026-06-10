package com.minseok.devboard.global.interceptor;

import com.minseok.devboard.global.common.SessionConst;
import com.minseok.devboard.member.entity.MemberStatus;
import com.minseok.devboard.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class LoginCheckInterceptor implements HandlerInterceptor {
    
    private final MemberRepository memberRepository;
    
    @Override
    public boolean preHandle(final HttpServletRequest request,
                             final HttpServletResponse response,
                             final Object handler) throws Exception {
        
        final HttpSession session = request.getSession(false);
        final Long loginMemberId = (session == null)
                                   ? null
                                   : (Long) session.getAttribute(SessionConst.LOGIN_MEMBER_ID);
        
        // 비로그인 시
        if (loginMemberId == null
                || !memberRepository.existsByIdAndStatus(loginMemberId, MemberStatus.ACTIVE)) {
            response.sendRedirect("/members/login?redirectURL="
                                          + getEncodedRedirectURL(request));
            return false;
        }
        
        return true;
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


















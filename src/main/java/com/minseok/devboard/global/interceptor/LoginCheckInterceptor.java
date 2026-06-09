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
        
        if (loginMemberId == null
                || !memberRepository.existsByIdAndStatus(loginMemberId, MemberStatus.ACTIVE)) {
            response.sendRedirect("/members/login?redirectURL=" + request.getRequestURI());
            
            return false;
        }
        
        return true;
    }
}


















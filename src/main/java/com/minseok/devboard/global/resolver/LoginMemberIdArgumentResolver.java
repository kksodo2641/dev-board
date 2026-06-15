package com.minseok.devboard.global.resolver;

import com.minseok.devboard.global.common.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class LoginMemberIdArgumentResolver implements HandlerMethodArgumentResolver {
    
    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMemberId.class)
                && parameter.getParameterType() == Long.class;
    }
    
    /**
     * - @LoginMemberId가 사용되는 컨트롤러 -> 로그인 필수 URL
     *
     * - 현재 정상 요청 흐름
     *   1. LoginCheckInterceptor -> LOGIN_MEMBER_ID 세션 존재 확인
     *   2. 세션 존재 시, Controller 진입 허용
     *   3. LoginMemberIdArgumentResolver -> loginMemberId 반환
     *
     * - 즉, LoginMemberIdArgumentResolver는 절대 null일 수 없음 (시스템 불변식)
     *    => null이라면, 버그 -> LoginCheckInterceptor 또는 적용 path 수정 필요
     */
    @Override
    public @Nullable Object resolveArgument(final MethodParameter parameter,
                                            @Nullable final ModelAndViewContainer mavContainer,
                                            final NativeWebRequest webRequest,
                                            @Nullable final WebDataBinderFactory binderFactory) throws Exception {
        
        final HttpServletRequest httpRequest = (HttpServletRequest) webRequest.getNativeRequest();
        final HttpSession session = httpRequest.getSession(false);
        
        final Long loginMemberId = session == null
                                   ? null
                                   : (Long) session.getAttribute(SessionConst.LOGIN_MEMBER_ID);
        
        if (loginMemberId == null) { // 시스템 불변식(invariant) 검증
            throw new IllegalStateException("@LoginMemberId requires login session.");
        }
        
        return loginMemberId;
    }
}


















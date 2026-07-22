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
     * 세션에서 로그인 회원 ID 조회
     *
     * @return 로그인 회원 ID, 선택적 조회에서 로그인하지 않은 경우 {@code null}
     * @throws IllegalStateException 필수 로그인 요청에 회원 ID가 없는 경우
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
        
        final LoginMemberId annotation = parameter.getParameterAnnotation(LoginMemberId.class);
        assert (annotation != null);
        
        // 시스템 불변식(invariant) 검증
        if (loginMemberId == null && annotation.required()) {
            throw new IllegalStateException("@LoginMemberId requires login session.");
        }
        
        return loginMemberId;
    }
}

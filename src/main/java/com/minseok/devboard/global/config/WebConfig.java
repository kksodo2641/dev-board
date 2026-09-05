package com.minseok.devboard.global.config;

import com.minseok.devboard.global.interceptor.LoginCheckInterceptor;
import com.minseok.devboard.global.resolver.LoginMemberIdArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    
    private final LoginCheckInterceptor loginCheckInterceptor;
    private final LoginMemberIdArgumentResolver loginMemberIdArgumentResolver;
    
    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(loginCheckInterceptor)
                .order(0)
                .addPathPatterns("/**")
                .excludePathPatterns("/error"); // 오류 디스패치의 인터셉터 적용 제외
    }
    
    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberIdArgumentResolver);
    }
}

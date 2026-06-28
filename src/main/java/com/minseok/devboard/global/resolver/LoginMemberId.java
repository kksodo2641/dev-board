package com.minseok.devboard.global.resolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)      // 메서드 파라미터에만 사용
@Retention(RetentionPolicy.RUNTIME) // Reflection 등을 활용 가능하도록 런타임까지 애노테이션 정보 유지
public @interface LoginMemberId {
}

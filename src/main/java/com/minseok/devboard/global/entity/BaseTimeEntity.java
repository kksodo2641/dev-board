package com.minseok.devboard.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseTimeEntity {
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 공백 문자열 검증
     *
     * @param fieldName 필드명
     * @param value     필드값
     * @throws NullPointerException     value가 null인 경우
     * @throws IllegalArgumentException value가 공백 문자(whitespace)로만 이루어진 경우
     */
    protected static void validateNotBlankText(final String fieldName, final String value) {
        assert (fieldName != null);
        
        requireNonNull(value);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "은 공백일 수 없습니다.");
        }
    }
}

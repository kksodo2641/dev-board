package com.minseok.devboard.global.exception.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ApiErrorCode {
    
    INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "요청 형식이 올바르지 않습니다."
    ),
    
    VALIDATION_ERROR(
            HttpStatus.BAD_REQUEST,
            "입력값이 올바르지 않습니다."
    ),
    
    LOGIN_REQUIRED(
            HttpStatus.UNAUTHORIZED,
            "로그인이 필요합니다."
    ),
    
    ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "해당 요청을 처리할 권한이 없습니다."
    ),
    
    MEMBER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "회원을 찾을 수 없습니다."
    ),
    
    BOARD_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "게시글을 찾을 수 없습니다."
    ),
    
    COMMENT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "댓글을 찾을 수 없습니다."
    ),
    
    REPLY_NOT_ALLOWED(
            HttpStatus.CONFLICT,
            "해당 댓글에 대댓글을 작성할 수 없습니다."
    ),
    
    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "서버 오류가 발생했습니다."
    );
    
    private final HttpStatus status;
    private final String message;
    
    public String getCode() {
        return name();
    }
}

package com.minseok.devboard.global.exception.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public final class ApiErrorResponse {
    
    private final String code;
    private final String message;
    
    public static ApiErrorResponse from(final ApiErrorCode apiErrorCode) {
        assert (apiErrorCode != null);
        
        return new ApiErrorResponse(apiErrorCode.getCode(),
                                    apiErrorCode.getMessage());
    }
    
    public static ApiErrorResponse of(final ApiErrorCode apiErrorCode,
                                      final String message) {
        assert (apiErrorCode != null);
        assert (message != null && !message.isBlank());
        
        return new ApiErrorResponse(apiErrorCode.getCode(),
                                    message);
    }
}

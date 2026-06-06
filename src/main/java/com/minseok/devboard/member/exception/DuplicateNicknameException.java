package com.minseok.devboard.member.exception;

public class DuplicateNicknameException extends RuntimeException {
    
    public DuplicateNicknameException() {
    }
    
    public DuplicateNicknameException(final String message) {
        super(message);
    }
    
    public DuplicateNicknameException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

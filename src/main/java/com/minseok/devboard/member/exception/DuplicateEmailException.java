package com.minseok.devboard.member.exception;

public class DuplicateEmailException extends RuntimeException {
    
    public DuplicateEmailException() {
    }
    
    public DuplicateEmailException(final String message) {
        super(message);
    }
    
    public DuplicateEmailException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

package com.minseok.devboard.member.exception;

public class LoginFailedException extends RuntimeException {
    
    public LoginFailedException() {
    }
    
    public LoginFailedException(final String message) {
        super(message);
    }
    
    public LoginFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

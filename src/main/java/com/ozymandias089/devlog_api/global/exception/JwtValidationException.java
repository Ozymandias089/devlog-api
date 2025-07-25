package com.ozymandias089.devlog_api.global.exception;

public class JwtValidationException extends RuntimeException{
    public JwtValidationException(String message) {
        super(message);
    }
    public JwtValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

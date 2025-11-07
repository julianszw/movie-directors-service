package com.example.eron_directors_service.exception;

public class InvalidParameterException extends RuntimeException {
    
    public InvalidParameterException(String message) {
        super(message);
    }
    
    public InvalidParameterException(String message, Throwable cause) {
        super(message, cause);
    }
}


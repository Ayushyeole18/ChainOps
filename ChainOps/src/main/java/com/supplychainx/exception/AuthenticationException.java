package com.supplychainx.exception;

/**
 * Exception thrown when authentication or role authorization fails.
 */
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}

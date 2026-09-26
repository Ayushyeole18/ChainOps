package com.supplychainx.exception;

/**
 * Base unchecked exception for database and JDBC operations.
 */
public class DatabaseException extends RuntimeException {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}

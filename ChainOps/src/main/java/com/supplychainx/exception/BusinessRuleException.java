package com.supplychainx.exception;

/**
 * Exception thrown when a supply chain business rule is violated
 * (e.g. insufficient inventory, receiving already received PO, duplicate items).
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}

package com.supplychainx.util;

import com.supplychainx.exception.ValidationException;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Enterprise validation utility enforcing business domain rules and constraints.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]*[(]{0,1}[0-9]{1,4}[)]{0,1}[-\\s./0-9]{6,20}$");
    private static final Pattern SKU_PATTERN = Pattern.compile("^[A-Z0-9_-]{3,50}$");

    public static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required and cannot be empty.");
        }
    }

    public static void validateSku(String sku) {
        requireNonEmpty(sku, "SKU");
        if (!SKU_PATTERN.matcher(sku.trim().toUpperCase()).matches()) {
            throw new ValidationException("SKU must be between 3 and 50 alphanumeric characters (hyphens and underscores allowed).");
        }
    }

    public static void validateEmail(String email) {
        requireNonEmpty(email, "Email");
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Invalid email format: " + email);
        }
    }

    public static void validatePhone(String phone) {
        if (phone != null && !phone.trim().isEmpty()) {
            if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
                throw new ValidationException("Invalid phone number format: " + phone);
            }
        }
    }

    public static void validatePrice(BigDecimal price, String fieldName) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException(fieldName + " cannot be negative.");
        }
    }

    public static void validatePositiveQuantity(int quantity, String fieldName) {
        if (quantity <= 0) {
            throw new ValidationException(fieldName + " must be greater than zero.");
        }
    }

    public static void validateNonNegativeQuantity(int quantity, String fieldName) {
        if (quantity < 0) {
            throw new ValidationException(fieldName + " cannot be negative.");
        }
    }
}

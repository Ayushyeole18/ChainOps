package com.supplychainx.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Enterprise password hashing utility using standard BCrypt.
 * Protects user credentials with adaptive work factor salt rounds.
 */
public class PasswordUtil {

    private static final int BCRYPT_LOG_ROUNDS = 12;

    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be blank");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_LOG_ROUNDS));
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}

package com.supplychainx.service;

import com.supplychainx.dao.UserDao;
import com.supplychainx.dao.UserDaoImpl;
import com.supplychainx.exception.AuthenticationException;
import com.supplychainx.model.Role;
import com.supplychainx.model.User;
import com.supplychainx.util.PasswordUtil;

import java.util.Optional;

public class AuthService {

    private final UserDao userDao;
    private static User currentUser;

    public AuthService() {
        this.userDao = new UserDaoImpl();
    }

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User authenticate(String emailOrUsername, String password) {
        if (emailOrUsername == null || emailOrUsername.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new AuthenticationException("Username/Email and password cannot be empty.");
        }

        Optional<User> userOpt = userDao.findByEmail(emailOrUsername.trim());
        if (userOpt.isEmpty()) {
            userOpt = userDao.findByUsername(emailOrUsername.trim());
        }

        if (userOpt.isEmpty()) {
            throw new AuthenticationException("Invalid credentials. Please check your username/email and password.");
        }

        User user = userOpt.get();

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new AuthenticationException("Account is currently " + user.getStatus() + ". Contact administrator.");
        }

        if (!PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid credentials. Password did not match.");
        }

        currentUser = user;
        return user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static boolean hasRole(Role... requiredRoles) {
        if (currentUser == null) return false;
        if (currentUser.getRole() == Role.ADMIN) return true; // Admins have full access
        for (Role role : requiredRoles) {
            if (currentUser.getRole() == role) return true;
        }
        return false;
    }
}

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

    public User registerUser(String fullName, String username, String email, String rawPassword, int roleId) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new AuthenticationException("Full name is required.");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new AuthenticationException("Username is required.");
        }
        if (email == null || !email.contains("@")) {
            throw new AuthenticationException("A valid corporate email is required.");
        }
        if (rawPassword == null || rawPassword.trim().length() < 6) {
            throw new AuthenticationException("Password must be at least 6 characters.");
        }

        String trimmedUser = username.trim().toLowerCase();
        String trimmedEmail = email.trim().toLowerCase();

        if (userDao.findByUsername(trimmedUser).isPresent()) {
            throw new AuthenticationException("Username '" + trimmedUser + "' is already taken.");
        }
        if (userDao.findByEmail(trimmedEmail).isPresent()) {
            throw new AuthenticationException("Email '" + trimmedEmail + "' is already registered.");
        }

        String passwordHash = PasswordUtil.hashPassword(rawPassword);
        User newUser = new User(0, trimmedUser, trimmedEmail, passwordHash, fullName.trim(), roleId, "ACTIVE");
        int generatedId = userDao.insert(newUser);
        newUser.setUserId(generatedId);

        currentUser = newUser;
        return newUser;
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

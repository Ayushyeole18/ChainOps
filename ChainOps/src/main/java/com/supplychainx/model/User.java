package com.supplychainx.model;

import java.sql.Timestamp;

public class User {
    private int userId;
    private String username;
    private String email;
    private String passwordHash;
    private String fullName;
    private int roleId;
    private Role role;
    private String status;
    private Timestamp createdAt;

    public User() {}

    public User(int userId, String username, String email, String passwordHash, String fullName, int roleId, String status) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.roleId = roleId;
        this.role = Role.fromId(roleId);
        this.status = status;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) {
        this.roleId = roleId;
        this.role = Role.fromId(roleId);
    }

    public Role getRole() { return role; }
    public void setRole(Role role) {
        this.role = role;
        if (role != null) this.roleId = role.getId();
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return fullName + " (" + (role != null ? role.getDisplayName() : "Unknown") + ")";
    }
}

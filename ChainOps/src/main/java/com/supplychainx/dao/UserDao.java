package com.supplychainx.dao;

import com.supplychainx.model.User;
import java.util.List;
import java.util.Optional;

public interface UserDao {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findById(int userId);
    List<User> findAll();
    int insert(User user);
    boolean update(User user);
    boolean updatePassword(int userId, String newPasswordHash);
    boolean delete(int userId);
}

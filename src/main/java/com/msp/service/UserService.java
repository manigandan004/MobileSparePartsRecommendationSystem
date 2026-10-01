package com.msp.service;

import com.msp.model.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    private final Map<String, User> users = new ConcurrentHashMap<>();

    public boolean register(String username, String password, String email) {
        validateRegistration(username, password, email);
        String key = username.trim().toLowerCase();
        if (users.containsKey(key)) return false;
        users.put(key, new User(username.trim(), password, email.trim()));
        return true;
    }

    public boolean userExists(String username) {
        return username != null && users.containsKey(username.trim().toLowerCase());
    }

    public int userCount() {
        return users.size();
    }

    public boolean authenticatePassword(String username, String password) {
        if (username == null || password == null) return false;
        User user = users.get(username.trim().toLowerCase());
        return user != null && user.getPassword().equals(password);
    }

    private void validateRegistration(String username, String password, String email) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must contain at least 6 characters");
        }
        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Valid email is required");
        }
    }
}

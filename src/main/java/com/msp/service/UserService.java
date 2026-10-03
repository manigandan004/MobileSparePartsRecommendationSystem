package com.msp.service;

import com.msp.model.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Business logic for user registration and user lookup. */
public class UserService {
    private final Map<String, User> users = new ConcurrentHashMap<>();

    public boolean register(String username, String password, String email) {
        validateRegistration(username, password, email);

        String usernameKey = username.trim().toLowerCase();
        String emailKey = email.trim().toLowerCase();

        if (users.containsKey(usernameKey)) return false;
        if (users.values().stream().anyMatch(user -> user.getEmail().equalsIgnoreCase(emailKey))) return false;

        users.put(usernameKey, new User(username.trim(), password, email.trim()));
        return true;
    }

    public boolean userExists(String username) {
        return username != null && users.containsKey(username.trim().toLowerCase());
    }

    public boolean emailExists(String email) {
        if (email == null) return false;
        String value = email.trim();
        return users.values().stream().anyMatch(user -> user.getEmail().equalsIgnoreCase(value));
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
        if (username.trim().length() < 3) {
            throw new IllegalArgumentException("Username must contain at least 3 characters");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must contain at least 6 characters");
        }
        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Valid email is required");
        }
    }
}

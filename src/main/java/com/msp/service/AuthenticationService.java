package com.msp.service;

public class AuthenticationService {
    private final UserService userService;

    public AuthenticationService(UserService userService) {
        if (userService == null) throw new IllegalArgumentException("UserService is required");
        this.userService = userService;
    }

    public boolean authenticate(String username, String password) {
        if (username == null || password == null) return false;
        if (!userService.userExists(username)) return false;
        return userService.authenticatePassword(username, password);
    }
}

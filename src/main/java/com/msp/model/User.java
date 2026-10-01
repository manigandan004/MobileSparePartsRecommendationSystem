package com.msp.model;

import java.util.Objects;

public final class User {
    private final String username;
    private final String password;
    private final String email;

    public User(String username, String password, String email) {
        this.username = Objects.requireNonNull(username, "username");
        this.password = Objects.requireNonNull(password, "password");
        this.email = Objects.requireNonNull(email, "email");
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
}

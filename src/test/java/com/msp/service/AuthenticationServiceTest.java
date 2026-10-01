package com.msp.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationServiceTest {

    @Test
    void validCredentialsAccepted() {
        UserService users = new UserService();
        users.register("manoj", "secret1", "manoj@example.com");
        AuthenticationService auth = new AuthenticationService(users);

        assertTrue(auth.authenticate("manoj", "secret1"));
    }

    @Test
    void wrongPasswordRejected() {
        UserService users = new UserService();
        users.register("manoj", "secret1", "manoj@example.com");
        AuthenticationService auth = new AuthenticationService(users);

        assertFalse(auth.authenticate("manoj", "wrong"));
    }

    @Test
    void unknownUserRejected() {
        AuthenticationService auth = new AuthenticationService(new UserService());

        assertFalse(auth.authenticate("missing", "secret1"));
    }

    @Test
    void nullCredentialsRejected() {
        AuthenticationService auth = new AuthenticationService(new UserService());

        assertFalse(auth.authenticate(null, null));
    }
}

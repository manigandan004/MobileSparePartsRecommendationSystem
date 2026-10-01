package com.msp.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Test
    void validRegistration() {
        UserService service = new UserService();
        assertTrue(service.register("manoj", "secret1", "manoj@example.com"));
        assertEquals(1, service.userCount());
    }

    @Test
    void duplicateRegistrationRejected() {
        UserService service = new UserService();
        service.register("manoj", "secret1", "manoj@example.com");
        assertFalse(service.register("manoj", "secret2", "other@example.com"));
        assertEquals(1, service.userCount());
    }

    @Test
    void shortPasswordRejected() {
        UserService service = new UserService();
        assertThrows(IllegalArgumentException.class,
                () -> service.register("user", "123", "user@example.com"));
    }

    @Test
    void blankUsernameRejected() {
        UserService service = new UserService();
        assertThrows(IllegalArgumentException.class,
                () -> service.register(" ", "secret1", "user@example.com"));
    }
}

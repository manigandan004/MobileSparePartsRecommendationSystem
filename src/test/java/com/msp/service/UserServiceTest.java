package com.msp.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Test
    void validRegistration() {
        UserService service = new UserService();
        assertTrue(service.register("manoj", "secret1", "manoj@example.com"));
        assertEquals(1, service.userCount());
        assertTrue(service.userExists("manoj"));
        assertTrue(service.emailExists("manoj@example.com"));
    }

    @Test
    void duplicateUsernameRejected() {
        UserService service = new UserService();
        service.register("manoj", "secret1", "manoj@example.com");
        assertFalse(service.register("manoj", "secret2", "other@example.com"));
        assertEquals(1, service.userCount());
    }

    @Test
    void duplicateEmailRejected() {
        UserService service = new UserService();
        service.register("manoj", "secret1", "manoj@example.com");
        assertFalse(service.register("other", "secret2", "MANOJ@example.com"));
        assertEquals(1, service.userCount());
    }

    @Test
    void shortPasswordRejected() {
        UserService service = new UserService();
        assertThrows(IllegalArgumentException.class,
                () -> service.register("user", "123", "user@example.com"));
    }

    @Test
    void shortUsernameRejected() {
        UserService service = new UserService();
        assertThrows(IllegalArgumentException.class,
                () -> service.register("ab", "secret1", "user@example.com"));
    }

    @Test
    void invalidEmailRejected() {
        UserService service = new UserService();
        assertThrows(IllegalArgumentException.class,
                () -> service.register("user", "secret1", "invalid-email"));
    }

    @Test
    void blankUsernameRejected() {
        UserService service = new UserService();
        assertThrows(IllegalArgumentException.class,
                () -> service.register(" ", "secret1", "user@example.com"));
    }
}

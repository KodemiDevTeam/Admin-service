package com.example.admin_service.config;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;

class SecurityRandomConfigTest {

    @Test
    void testSecureRandomBean() {
        SecurityRandomConfig config = new SecurityRandomConfig();

        SecureRandom secureRandom = config.secureRandom();

        assertNotNull(secureRandom);
        assertTrue(secureRandom instanceof SecureRandom);
    }

    @Test
    void testSecureRandomGeneratesRandomNumbers() {
        SecurityRandomConfig config = new SecurityRandomConfig();

        SecureRandom secureRandom = config.secureRandom();

        int random = secureRandom.nextInt();

        // Verifies that the method executes without throwing an exception
        assertNotNull(secureRandom);
    }
}
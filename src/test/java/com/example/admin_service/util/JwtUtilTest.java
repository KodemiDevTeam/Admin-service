package com.example.admin_service.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JwtUtilTest {

    private static final String secret = "my-32-character-ultra-secure-and-ultra-long-secret";
    private static final String TEST_USER_ID = "user-100";
    private static final String TEST_EMAIL = "test@domain.com";
    private static final String TEST_ROLE = "ADMIN";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKeyString", secret);
    }

    private String createTestToken(String userId, String email, String role) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void testExtractUserId() {
        String token = createTestToken(TEST_USER_ID, TEST_EMAIL, TEST_ROLE);
        assertEquals(TEST_USER_ID, jwtUtil.extractUserId(token));
    }

    @Test
    void testExtractEmail() {
        String token = createTestToken(TEST_USER_ID, TEST_EMAIL, TEST_ROLE);
        assertEquals(TEST_EMAIL, jwtUtil.extractEmail("Bearer " + token));
    }

    @Test
    void testExtractRole() {
        String token = createTestToken(TEST_USER_ID, TEST_EMAIL, TEST_ROLE);
        assertEquals(TEST_ROLE, jwtUtil.extractRole(token));
    }
}
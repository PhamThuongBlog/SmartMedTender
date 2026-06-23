package com.medbid.auth;

import com.medbid.auth.entity.Role;
import com.medbid.auth.entity.User;
import com.medbid.auth.security.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        SecretKey key = Keys.hmacShaKeyFor(Base64.getEncoder().encode(
                "test-secret-key-that-is-long-enough-for-hmac-sha-256-algorithm!!".getBytes()));
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", Base64.getEncoder().encodeToString(key.getEncoded()));
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpiration", 3600000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenExpiration", 86400000L);
        // Re-invoke @PostConstruct init() since jwtSecret was just set
        jwtUtil.init();
    }

    @Test
    void shouldGenerateAndValidateAccessToken() {
        User user = createTestUser();

        String token = jwtUtil.generateAccessToken(user);
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
        assertEquals("admin", jwtUtil.extractUsername(token));
    }

    @Test
    void shouldGenerateAndValidateRefreshToken() {
        User user = createTestUser();

        String token = jwtUtil.generateRefreshToken(user);
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void shouldThrowExceptionForInvalidToken() {
        assertThrows(Exception.class, () -> jwtUtil.validateToken("invalid.token.here"));
    }

    private User createTestUser() {
        User user = new User();
        user.setId(java.util.UUID.randomUUID());
        user.setUsername("admin");
        user.setPassword("encoded");
        user.setEnabled(true);
        Role role = new Role();
        role.setName("ADMIN");
        user.setRole(role);
        return user;
    }
}

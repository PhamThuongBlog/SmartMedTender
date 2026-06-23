package com.medbid.auth;

import com.medbid.auth.dto.LoginRequest;
import com.medbid.auth.dto.LoginResponse;
import com.medbid.auth.dto.RegisterRequest;
import com.medbid.auth.entity.Role;
import com.medbid.auth.entity.User;
import com.medbid.auth.mapper.UserMapper;
import com.medbid.auth.repository.*;
import com.medbid.auth.security.JwtUtil;
import com.medbid.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private LoginHistoryRepository loginHistoryRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private UserMapper userMapper;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, roleRepository,
                refreshTokenRepository, loginHistoryRepository,
                passwordEncoder, jwtUtil, userMapper);
    }

    @Test
    void shouldLoginSuccessfully() {
        User user = createUser();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(jwtUtil.generateAccessToken(user)).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(user)).thenReturn("refresh-token");

        LoginResponse response = authService.login(
                new LoginRequest("admin", "password"), "127.0.0.1", "test-agent");

        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        verify(loginHistoryRepository).save(any());
    }

    @Test
    void shouldRegisterUser() {
        UUID roleId = UUID.randomUUID();
        Role role = new Role();
        role.setId(roleId);
        role.setName("STAFF");
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(UUID.randomUUID());
            return savedUser;
        });

        authService.register(new RegisterRequest("newuser", "Password123!", "new@test.com",
                "New User", "0123456789", roleId));

        verify(userRepository).save(any(User.class));
    }

    private User createUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("admin");
        user.setPassword("encoded");
        user.setEnabled(true);
        user.setAccountLocked(false);
        user.setFailedAttempts(0);
        Role role = new Role();
        role.setName("ADMIN");
        user.setRole(role);
        return user;
    }
}

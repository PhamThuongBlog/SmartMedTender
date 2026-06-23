package com.medbid.auth.service;

import com.medbid.auth.dto.ChangePasswordRequest;
import com.medbid.auth.dto.LoginRequest;
import com.medbid.auth.dto.LoginResponse;
import com.medbid.auth.dto.RegisterRequest;
import com.medbid.auth.dto.UserDto;
import com.medbid.auth.entity.LoginHistory;
import com.medbid.auth.entity.RefreshToken;
import com.medbid.auth.entity.Role;
import com.medbid.auth.entity.User;
import com.medbid.auth.mapper.UserMapper;
import com.medbid.auth.repository.LoginHistoryRepository;
import com.medbid.auth.repository.RefreshTokenRepository;
import com.medbid.auth.repository.RoleRepository;
import com.medbid.auth.repository.UserRepository;
import com.medbid.auth.security.JwtUtil;
import com.medbid.exception.BusinessException;
import com.medbid.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    private static final int MAX_FAILED_ATTEMPTS = 5;

    @Transactional(noRollbackFor = {BadCredentialsException.class, BusinessException.class})
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        String username = request.username();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (user.getAccountLocked()) {
            recordLoginFailure(user.getId(), username, ipAddress, userAgent,
                    "Account is locked due to too many failed attempts");
            throw new BusinessException("Account is locked. Please contact your administrator.");
        }

        if (!user.getEnabled()) {
            recordLoginFailure(user.getId(), username, ipAddress, userAgent, "Account is disabled");
            throw new BusinessException("Account is disabled.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            if (user.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.setAccountLocked(true);
                recordLoginFailure(user.getId(), username, ipAddress, userAgent,
                        "Account locked after " + MAX_FAILED_ATTEMPTS + " failed attempts");
                userRepository.save(user);
                throw new BusinessException(
                        "Account has been locked due to " + MAX_FAILED_ATTEMPTS + " failed login attempts.");
            }
            userRepository.save(user);
            recordLoginFailure(user.getId(), username, ipAddress, userAgent, "Invalid password");
            throw new BadCredentialsException("Invalid username or password");
        }

        user.setFailedAttempts(0);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        recordLoginSuccess(user.getId(), username, ipAddress, userAgent);

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        saveRefreshToken(user, refreshToken);

        log.info("User '{}' logged in successfully", username);
        return new LoginResponse(accessToken, refreshToken, jwtUtil.getAccessTokenExpiration());
    }

    @Transactional
    public LoginResponse refreshToken(String token) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Invalid refresh token"));

        if (!storedToken.isValid()) {
            refreshTokenRepository.delete(storedToken);
            throw new BusinessException("Refresh token has expired or been revoked");
        }

        if (jwtUtil.isTokenExpired(token)) {
            refreshTokenRepository.delete(storedToken);
            throw new BusinessException("Refresh token has expired");
        }

        User user = storedToken.getUser();

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        String newAccessToken = jwtUtil.generateAccessToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);

        saveRefreshToken(user, newRefreshToken);

        log.info("Token refreshed for user '{}'", user.getUsername());
        return new LoginResponse(newAccessToken, newRefreshToken, jwtUtil.getAccessTokenExpiration());
    }

    @Transactional
    public void logout(String refreshToken, UUID userId) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });

        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("User '{}' logged out, all refresh tokens revoked", userId);
    }

    @Transactional
    public UserDto register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException("Username already exists: " + request.username());
        }
        if (request.email() != null && userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already exists: " + request.email());
        }

        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + request.roleId()));

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .email(request.email())
                .fullName(request.fullName())
                .phone(request.phone())
                .role(role)
                .enabled(true)
                .accountLocked(false)
                .failedAttempts(0)
                .mfaEnabled(false)
                .passwordChangedAt(LocalDateTime.now())
                .build();

        user.setDeleted(false);
        user = userRepository.save(user);

        log.info("New user registered: '{}' with role '{}'", user.getUsername(), role.getName());
        return userMapper.toDto(user);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BusinessException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        refreshTokenRepository.revokeAllByUserId(userId);

        log.info("Password changed for user '{}'", user.getUsername());
    }

    private void saveRefreshToken(User user, String token) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTokenExpiration() / 1000))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    private void recordLoginSuccess(UUID userId, String username, String ipAddress, String userAgent) {
        LoginHistory history = LoginHistory.builder()
                .userId(userId)
                .username(username)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(true)
                .build();
        loginHistoryRepository.save(history);
    }

    private void recordLoginFailure(UUID userId, String username, String ipAddress, String userAgent,
                                     String failureReason) {
        LoginHistory history = LoginHistory.builder()
                .userId(userId)
                .username(username)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(false)
                .failureReason(failureReason)
                .build();
        loginHistoryRepository.save(history);
    }
}

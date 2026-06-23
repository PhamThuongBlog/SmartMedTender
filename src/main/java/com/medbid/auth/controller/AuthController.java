package com.medbid.auth.controller;

import com.medbid.auth.dto.ChangePasswordRequest;
import com.medbid.auth.dto.LoginRequest;
import com.medbid.auth.dto.LoginResponse;
import com.medbid.auth.dto.RegisterRequest;
import com.medbid.auth.dto.TokenRefreshRequest;
import com.medbid.auth.dto.UserDto;
import com.medbid.auth.entity.User;
import com.medbid.auth.mapper.UserMapper;
import com.medbid.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                                HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        log.info("Login attempt for user: '{}' from IP: {}", request.username(), ipAddress);

        LoginResponse response = authService.login(request, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        log.info("Token refresh requested");
        LoginResponse response = authService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody TokenRefreshRequest request,
                                        @AuthenticationPrincipal User currentUser) {
        log.info("Logout requested for user: '{}'", currentUser.getUsername());
        authService.logout(request.refreshToken(), currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest request) {
        log.info("User registration requested for username: '{}'", request.username());
        UserDto created = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        log.debug("Current user info requested for: '{}'", currentUser.getUsername());
        return ResponseEntity.ok(userMapper.toDto(currentUser));
    }

    @PatchMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                @AuthenticationPrincipal User currentUser) {
        log.info("Password change requested for user: '{}'", currentUser.getUsername());
        authService.changePassword(currentUser.getId(), request);
        return ResponseEntity.noContent().build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }
}

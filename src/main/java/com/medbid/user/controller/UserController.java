package com.medbid.user.controller;

import com.medbid.auth.dto.RegisterRequest;
import com.medbid.auth.dto.UserDto;
import com.medbid.auth.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Page<UserDto>> getAll(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("Fetching users page={}, size={}, search='{}'",
                pageable.getPageNumber(), pageable.getPageSize(), search);
        return ResponseEntity.ok(userService.getAll(search, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable UUID id) {
        log.debug("Fetching user by id: {}", id);
        return ResponseEntity.ok(userService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserDto> create(@Valid @RequestBody RegisterRequest request) {
        log.info("Creating user: '{}'", request.username());
        UserDto created = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserDto> update(@PathVariable UUID id,
                                           @Valid @RequestBody UserDto request) {
        log.info("Updating user id: {}", id);
        UserDto updated = userService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/lock")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserDto> toggleLock(@PathVariable UUID id) {
        log.info("Toggling lock status for user id: {}", id);
        UserDto updated = userService.lockAccount(id);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> resetPassword(@PathVariable UUID id,
                                               @RequestBody Map<String, @Size(min = 8, message = "Password must be at least 8 characters") String> body) {
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        log.info("Admin resetting password for user id: {}", id);
        userService.resetPassword(id, newPassword);
        return ResponseEntity.noContent().build();
    }
}

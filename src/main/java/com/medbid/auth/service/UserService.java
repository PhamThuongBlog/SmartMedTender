package com.medbid.auth.service;

import com.medbid.auth.dto.RegisterRequest;
import com.medbid.auth.dto.UserDto;
import com.medbid.auth.entity.Role;
import com.medbid.auth.entity.User;
import com.medbid.auth.mapper.UserMapper;
import com.medbid.auth.repository.RoleRepository;
import com.medbid.auth.repository.UserRepository;
import com.medbid.exception.BusinessException;
import com.medbid.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserDto getById(UUID id) {
        User user = userRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public Page<UserDto> getAll(String search, Pageable pageable) {
        if (search == null || search.isBlank()) {
            return userRepository.findByDeletedFalse(pageable).map(userMapper::toDto);
        }
        return userRepository.findAllActive(search, pageable).map(userMapper::toDto);
    }

    public UserDto create(RegisterRequest request) {
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

        log.info("User created: '{}' with role '{}'", user.getUsername(), role.getName());
        return userMapper.toDto(user);
    }

    public UserDto update(UUID id, UserDto dto) {
        User user = userRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (dto.email() != null && !dto.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(dto.email())) {
                throw new BusinessException("Email already exists: " + dto.email());
            }
            user.setEmail(dto.email());
        }
        if (dto.fullName() != null) {
            user.setFullName(dto.fullName());
        }
        if (dto.phone() != null) {
            user.setPhone(dto.phone());
        }
        if (dto.avatarUrl() != null) {
            user.setAvatarUrl(dto.avatarUrl());
        }
        if (dto.roleId() != null) {
            Role role = roleRepository.findById(dto.roleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + dto.roleId()));
            user.setRole(role);
        }
        if (dto.enabled() != null) {
            user.setEnabled(dto.enabled());
        }

        user = userRepository.save(user);

        log.info("User updated: '{}' (id={})", user.getUsername(), user.getId());
        return userMapper.toDto(user);
    }

    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setDeleted(true);
        userRepository.save(user);
        log.info("User soft-deleted: '{}' (id={})", user.getUsername(), id);
    }

    public UserDto lockAccount(UUID id) {
        User user = userRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setAccountLocked(!user.getAccountLocked());
        if (user.getAccountLocked()) {
            user.setFailedAttempts(0);
        }
        user = userRepository.save(user);

        log.info("User '{}' account lock status: {}", user.getUsername(), user.getAccountLocked());
        return userMapper.toDto(user);
    }

    public UserDto unlockAccount(UUID id) {
        User user = userRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setAccountLocked(false);
        user.setFailedAttempts(0);
        user = userRepository.save(user);

        log.info("User '{}' account unlocked", user.getUsername());
        return userMapper.toDto(user);
    }

    public void resetPassword(UUID id, String newPassword) {
        User user = userRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Password reset for user '{}' by admin", user.getUsername());
    }

    @Transactional(readOnly = true)
    public UserDto getByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public User getUserEntityById(UUID id) {
        return userRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
}

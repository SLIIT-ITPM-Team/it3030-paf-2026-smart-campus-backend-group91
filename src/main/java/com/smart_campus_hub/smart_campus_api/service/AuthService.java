package com.smart_campus_hub.smart_campus_api.service;

import com.smart_campus_hub.smart_campus_api.dto.auth.AuthResponse;
import com.smart_campus_hub.smart_campus_api.dto.auth.LoginRequest;
import com.smart_campus_hub.smart_campus_api.dto.auth.RegisterRequest;
import com.smart_campus_hub.smart_campus_api.dto.auth.UserResponse;
import com.smart_campus_hub.smart_campus_api.entity.Role;
import com.smart_campus_hub.smart_campus_api.entity.RoleEntity;
import com.smart_campus_hub.smart_campus_api.entity.User;
import com.smart_campus_hub.smart_campus_api.exception.ApiException;
import com.smart_campus_hub.smart_campus_api.repository.RoleRepository;
import com.smart_campus_hub.smart_campus_api.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, Long> tokenStore = new ConcurrentHashMap<>();

    public AuthService(
        UserRepository userRepository,
        RoleRepository roleRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(RegisterRequest request) {
        String username = normalize(request.getUsername());
        String email = normalize(request.getEmail()).toLowerCase();

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ApiException(HttpStatus.CONFLICT, "Duplicate username.");
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Duplicate email.");
        }

        User user = new User();
        user.setName(username);
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setRoleId(resolveRoleId(request.getRole()));
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        return toUserResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        String username = normalize(request.getUsername());

        User user = userRepository
            .findByUsernameIgnoreCase(username)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Wrong password.");
        }

        String accessToken = UUID.randomUUID().toString();
        tokenStore.put(accessToken, user.getId());

        return new AuthResponse(accessToken, toUserResponse(user));
    }

    public UserResponse getCurrentUser(String accessToken) {
        Long userId = tokenStore.get(accessToken);
        if (userId == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid or expired token.");
        }

        User user = userRepository
            .findById(userId)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid or expired token."));

        return toUserResponse(user);
    }

    public void logout(String accessToken) {
        tokenStore.remove(accessToken);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            List.of(user.getRole().name())
        );
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private Integer resolveRoleId(Role role) {
        String roleName = role.name();
        return roleRepository
            .findByRoleNameIgnoreCase(roleName)
            .map(RoleEntity::getRoleId)
            .orElseGet(() -> {
                RoleEntity newRole = new RoleEntity();
                newRole.setRoleName(roleName);
                newRole.setDescription(roleName + " role");
                return roleRepository.save(newRole).getRoleId();
            });
    }
}

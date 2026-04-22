package com.smart_campus_hub.smart_campus_api.service;

import com.smart_campus_hub.smart_campus_api.dto.auth.AuthResponse;
import com.smart_campus_hub.smart_campus_api.dto.auth.AdminCreateUserRequest;
import com.smart_campus_hub.smart_campus_api.dto.auth.LoginRequest;
import com.smart_campus_hub.smart_campus_api.dto.auth.RegisterRequest;
import com.smart_campus_hub.smart_campus_api.dto.auth.UserResponse;
import com.smart_campus_hub.smart_campus_api.entity.Role;
import com.smart_campus_hub.smart_campus_api.entity.RoleEntity;
import com.smart_campus_hub.smart_campus_api.entity.User;
import com.smart_campus_hub.smart_campus_api.exception.ApiException;
import com.smart_campus_hub.smart_campus_api.repository.RoleRepository;
import com.smart_campus_hub.smart_campus_api.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
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
        String identifier = normalize(request.getUsername());

        User user = userRepository
            .findByUsernameIgnoreCaseOrEmailIgnoreCase(identifier, identifier)
            .orElseThrow(this::invalidCredentialsException);

        if (!user.isEnabled()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "User account is disabled.");
        }

        if (!verifyAndUpgradePassword(user, request.getPassword())) {
            throw invalidCredentialsException();
        }

        String accessToken = UUID.randomUUID().toString();
        tokenStore.put(accessToken, user.getId());

        return new AuthResponse(accessToken, toUserResponse(user));
    }

    public UserResponse getCurrentUser(String accessToken) {
        User user = getUserByAccessToken(accessToken);
        return toUserResponse(user);
    }

    public List<UserResponse> getAllUsersForDashboard(String accessToken) {
        if (accessToken != null && !accessToken.isBlank()) {
            requireAdmin(accessToken);
        }

        return userRepository
            .findAll()
            .stream()
            .sorted(Comparator.comparing(User::getId))
            .map(this::toUserResponse)
            .toList();
    }

    public UserResponse createUserForAdmin(String accessToken, AdminCreateUserRequest request) {
        requireAdmin(accessToken);

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
        user.setPasswordHash(passwordEncoder.encode(generateTemporaryPassword()));
        user.setRole(request.getRole());
        user.setRoleId(resolveRoleId(request.getRole()));
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        return toUserResponse(savedUser);
    }

    public void logout(String accessToken) {
        tokenStore.remove(accessToken);
    }

    private UserResponse toUserResponse(User user) {
        String roleName = resolveRoleName(user);
        String displayName = normalize(user.getName()).isBlank() ? user.getUsername() : user.getName();

        return new UserResponse(
            user.getId(),
            user.getId(),
            displayName,
            user.getUsername(),
            user.getEmail(),
            roleName,
            List.of(roleName),
            user.isEnabled()
        );
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private User getUserByAccessToken(String accessToken) {
        Long userId = tokenStore.get(accessToken);
        if (userId == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid or expired token.");
        }

        return userRepository
            .findById(userId)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid or expired token."));
    }

    private User requireAdmin(String accessToken) {
        User currentUser = getUserByAccessToken(accessToken);
        if (!Role.ADMIN.name().equals(resolveRoleName(currentUser))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin access required.");
        }

        return currentUser;
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

    private ApiException invalidCredentialsException() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
    }

    private boolean verifyAndUpgradePassword(User user, String rawPassword) {
        String storedPassword = user.getPasswordHash();

        if (rawPassword == null || rawPassword.isBlank() || storedPassword == null || storedPassword.isBlank()) {
            return false;
        }

        if (looksLikeBcryptHash(storedPassword)) {
            try {
                return passwordEncoder.matches(rawPassword, storedPassword);
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }

        if (!storedPassword.equals(rawPassword)) {
            return false;
        }

        // Upgrade legacy plain-text passwords after a successful login.
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
        return true;
    }

    private boolean looksLikeBcryptHash(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }

    private String generateTemporaryPassword() {
        return "Temp@" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private String resolveRoleName(User user) {
        if (user.getRole() != null) {
            return user.getRole().name();
        }

        if (user.getRoleId() != null) {
            return roleRepository
                .findById(user.getRoleId())
                .map(RoleEntity::getRoleName)
                .filter(name -> !name.isBlank())
                .map(name -> name.toUpperCase(Locale.ROOT))
                .orElse(Role.USER.name());
        }

        return Role.USER.name();
    }
}

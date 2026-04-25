package com.smart_campus_hub.smart_campus_api.service;

import com.smart_campus_hub.smart_campus_api.dto.auth.AuthResponse;
import com.smart_campus_hub.smart_campus_api.dto.auth.DeleteUserResponse;
import com.smart_campus_hub.smart_campus_api.dto.auth.LoginRequest;
import com.smart_campus_hub.smart_campus_api.dto.auth.RegisterRequest;
import com.smart_campus_hub.smart_campus_api.dto.auth.UpdateUserRoleRequest;
import com.smart_campus_hub.smart_campus_api.dto.auth.UserResponse;
import com.smart_campus_hub.smart_campus_api.entity.Role;
import com.smart_campus_hub.smart_campus_api.entity.RoleEntity;
import com.smart_campus_hub.smart_campus_api.entity.User;
import com.smart_campus_hub.smart_campus_api.exception.ApiException;
import com.smart_campus_hub.smart_campus_api.repository.RoleRepository;
import com.smart_campus_hub.smart_campus_api.repository.UserRepository;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
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

    public UserResponse getUserById(Long userId) {
        return userRepository.findById(userId).map(this::toUserResponse).orElse(null);
    }

    public List<UserResponse> getAllUsersForDashboard(String accessToken) {
        requireAdmin(accessToken);

        return userRepository
            .findAll()
            .stream()
            .sorted(Comparator.comparing(User::getId))
            .map(this::toUserResponse)
            .toList();
    }

    public UserResponse updateUserRole(String accessToken, Long targetUserId, UpdateUserRoleRequest request) {
        requireAdmin(accessToken);

        User user = userRepository
            .findById(targetUserId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));

        Role targetRole = parseRole(request.getRole());
        Role currentRole = resolveRole(user);

        if (currentRole == Role.ADMIN && targetRole != Role.ADMIN && userRepository.countByRole(Role.ADMIN) <= 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot change role of the last ADMIN user.");
        }

        user.setRole(targetRole);
        user.setRoleId(resolveRoleId(targetRole));

        User savedUser = userRepository.save(user);
        return toUserResponse(savedUser);
    }

    public DeleteUserResponse deleteUser(String accessToken, Long targetUserId) {
        User currentAdmin = requireAdmin(accessToken);

        User user = userRepository
            .findById(targetUserId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));

        if (currentAdmin.getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Admin users cannot delete their own account.");
        }

        if (resolveRole(user) == Role.ADMIN && userRepository.countByRole(Role.ADMIN) <= 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot delete the last ADMIN user.");
        }

        userRepository.delete(user);
        tokenStore.values().removeIf(userId -> user.getId().equals(userId));

        return new DeleteUserResponse(user.getId(), "User deleted successfully.");
    }

    public void logout(String accessToken) {
        tokenStore.remove(accessToken);
    }

    public String loginOrRegisterOAuthUser(String email, String name) {
        String normalizedEmail = email.toLowerCase().trim();

        User user = userRepository
            .findByUsernameIgnoreCaseOrEmailIgnoreCase(normalizedEmail, normalizedEmail)
            .orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(normalizedEmail);
                String username = generateUniqueUsername(normalizedEmail);
                newUser.setUsername(username);
                newUser.setName(name != null && !name.isBlank() ? name : username);
                newUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
                newUser.setRole(Role.USER);
                newUser.setRoleId(resolveRoleId(Role.USER));
                newUser.setEnabled(true);
                return userRepository.save(newUser);
            });

        String token = UUID.randomUUID().toString();
        tokenStore.put(token, user.getId());
        return token;
    }

    private String generateUniqueUsername(String email) {
        String base = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "_");
        if (!userRepository.existsByUsernameIgnoreCase(base)) return base;
        for (int i = 1; i < 1000; i++) {
            String candidate = base + i;
            if (!userRepository.existsByUsernameIgnoreCase(candidate)) return candidate;
        }
        return base + UUID.randomUUID().toString().substring(0, 6);
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

    private Role resolveRole(User user) {
        String roleName = resolveRoleName(user);
        try {
            return Role.valueOf(roleName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return Role.USER;
        }
    }

    private Role parseRole(String rawRole) {
        String normalizedRole = normalize(rawRole).toUpperCase(Locale.ROOT);
        if (normalizedRole.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Role is required.");
        }

        try {
            return Role.valueOf(normalizedRole);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(
                HttpStatus.BAD_REQUEST,
                "Invalid role. Allowed roles: " + allowedRoles() + "."
            );
        }
    }

    private String allowedRoles() {
        return Arrays.stream(Role.values()).map(Role::name).collect(Collectors.joining(", "));
    }
}

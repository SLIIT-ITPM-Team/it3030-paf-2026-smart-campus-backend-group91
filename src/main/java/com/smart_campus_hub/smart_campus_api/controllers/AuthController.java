package com.smart_campus_hub.smart_campus_api.controllers;

import com.smart_campus_hub.smart_campus_api.dto.auth.AuthResponse;
import com.smart_campus_hub.smart_campus_api.dto.auth.LoginRequest;
import com.smart_campus_hub.smart_campus_api.dto.auth.RegisterRequest;
import com.smart_campus_hub.smart_campus_api.dto.auth.UserResponse;
import com.smart_campus_hub.smart_campus_api.entity.Role;
import com.smart_campus_hub.smart_campus_api.entity.User;
import com.smart_campus_hub.smart_campus_api.exception.ApiException;
import com.smart_campus_hub.smart_campus_api.repository.UserRepository;
import com.smart_campus_hub.smart_campus_api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse created = authService.register(request);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserResponse me(@RequestHeader(name = "Authorization", required = false) String authorization) {
        return authService.getCurrentUser(extractRequiredToken(authorization));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        @RequestHeader(name = "Authorization", required = false) String authorization
    ) {
        String token = extractOptionalToken(authorization);
        if (token != null) {
            authService.logout(token);
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/technicians")
    public ResponseEntity<List<Map<String, Object>>> getTechnicians() {
        List<Map<String, Object>> technicians = userRepository.findByRole(Role.TECHNICIAN).stream()
                .map(u -> Map.<String, Object>of(
                        "id", u.getId(),
                        "username", u.getUsername(),
                        "email", u.getEmail()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(technicians);
    }

    private String extractRequiredToken(String authorization) {
        String token = extractOptionalToken(authorization);
        if (token == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header.");
        }

        return token;
    }

    private String extractOptionalToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }

        if (!authorization.startsWith(BEARER_PREFIX) || authorization.length() <= BEARER_PREFIX.length()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header.");
        }

        return authorization.substring(BEARER_PREFIX.length()).trim();
    }
}

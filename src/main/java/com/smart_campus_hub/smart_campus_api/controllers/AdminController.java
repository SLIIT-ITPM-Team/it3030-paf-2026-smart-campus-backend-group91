package com.smart_campus_hub.smart_campus_api.controllers;

import com.smart_campus_hub.smart_campus_api.dto.auth.AdminCreateUserRequest;
import com.smart_campus_hub.smart_campus_api.dto.auth.UserResponse;
import com.smart_campus_hub.smart_campus_api.exception.ApiException;
import com.smart_campus_hub.smart_campus_api.service.AuthService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;

    public AdminController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getUsers(
        @RequestHeader(name = "Authorization", required = false) String authorization
    ) {
        List<UserResponse> users = authService.getAllUsersForAdmin(extractRequiredToken(authorization));
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(
        @RequestHeader(name = "Authorization", required = false) String authorization,
        @Valid @RequestBody AdminCreateUserRequest request
    ) {
        UserResponse createdUser = authService.createUserForAdmin(extractRequiredToken(authorization), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    private String extractRequiredToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header.");
        }

        if (!authorization.startsWith(BEARER_PREFIX) || authorization.length() <= BEARER_PREFIX.length()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header.");
        }

        return authorization.substring(BEARER_PREFIX.length()).trim();
    }
}

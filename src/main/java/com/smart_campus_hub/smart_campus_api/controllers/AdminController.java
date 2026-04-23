package com.smart_campus_hub.smart_campus_api.controllers;

import com.smart_campus_hub.smart_campus_api.dto.auth.DeleteUserResponse;
import com.smart_campus_hub.smart_campus_api.dto.auth.UpdateUserRoleRequest;
import com.smart_campus_hub.smart_campus_api.dto.auth.UserResponse;
import com.smart_campus_hub.smart_campus_api.exception.ApiException;
import com.smart_campus_hub.smart_campus_api.service.AuthService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
        List<UserResponse> users = authService.getAllUsersForDashboard(extractRequiredToken(authorization));
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(
        @PathVariable Long id,
        @RequestHeader(name = "Authorization", required = false) String authorization,
        @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        UserResponse updatedUser = authService.updateUserRole(extractRequiredToken(authorization), id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<DeleteUserResponse> deleteUser(
        @PathVariable Long id,
        @RequestHeader(name = "Authorization", required = false) String authorization
    ) {
        DeleteUserResponse response = authService.deleteUser(extractRequiredToken(authorization), id);
        return ResponseEntity.ok(response);
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
            return null;
        }

        return authorization.substring(BEARER_PREFIX.length()).trim();
    }
}

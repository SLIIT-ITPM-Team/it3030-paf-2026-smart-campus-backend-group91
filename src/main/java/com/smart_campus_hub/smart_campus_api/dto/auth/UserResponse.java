package com.smart_campus_hub.smart_campus_api.dto.auth;

import java.util.List;

public record UserResponse(
    Long id,
    Long userId,
    String name,
    String username,
    String email,
    String role,
    List<String> roles,
    boolean enabled
) {}


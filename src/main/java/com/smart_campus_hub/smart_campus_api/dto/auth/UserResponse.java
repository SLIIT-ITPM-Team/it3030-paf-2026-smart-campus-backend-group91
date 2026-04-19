package com.smart_campus_hub.smart_campus_api.dto.auth;

import java.util.List;

public record UserResponse(
    Long id,
    String username,
    String email,
    List<String> roles
) {}


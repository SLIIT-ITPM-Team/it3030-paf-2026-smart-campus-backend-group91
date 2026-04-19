package com.smart_campus_hub.smart_campus_api.dto.auth;

public record AuthResponse(
    String accessToken,
    UserResponse user
) {}


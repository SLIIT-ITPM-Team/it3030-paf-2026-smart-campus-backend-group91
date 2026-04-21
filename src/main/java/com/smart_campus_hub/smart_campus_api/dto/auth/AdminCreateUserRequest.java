package com.smart_campus_hub.smart_campus_api.dto.auth;

import com.smart_campus_hub.smart_campus_api.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AdminCreateUserRequest {
    @NotBlank(message = "Username is required.")
    @Size(max = 100, message = "Username is too long.")
    private String username;

    @NotBlank(message = "Email is required.")
    @Email(message = "Email is invalid.")
    @Size(max = 150, message = "Email is too long.")
    private String email;

    @NotNull(message = "Role is required.")
    private Role role;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}

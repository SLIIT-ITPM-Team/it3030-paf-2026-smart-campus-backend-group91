package com.smart_campus_hub.smart_campus_api.controllers;

import com.smart_campus_hub.smart_campus_api.dto.EmailNotificationDto;
import com.smart_campus_hub.smart_campus_api.service.EmailNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/email-notifications")
public class EmailNotificationController {

    @Autowired
    private EmailNotificationService emailNotificationService;

    @GetMapping
    public ResponseEntity<List<EmailNotificationDto>> getEmails(
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String userRole) {
        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            throw new org.springframework.security.access.AccessDeniedException("Only admins can view email notifications.");
        }
        return ResponseEntity.ok(emailNotificationService.getAll());
    }
}

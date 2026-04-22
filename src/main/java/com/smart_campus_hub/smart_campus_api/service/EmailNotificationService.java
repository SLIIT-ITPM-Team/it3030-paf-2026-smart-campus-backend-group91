package com.smart_campus_hub.smart_campus_api.service;

import com.smart_campus_hub.smart_campus_api.dto.EmailNotificationDto;
import com.smart_campus_hub.smart_campus_api.model.EmailNotification;
import com.smart_campus_hub.smart_campus_api.repository.EmailNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmailNotificationService {

    @Autowired
    private EmailNotificationRepository emailNotificationRepository;

    public void send(String recipient, String subject, String body) {
        try {
            emailNotificationRepository.save(EmailNotification.builder()
                    .recipient(recipient)
                    .subject(subject)
                    .body(body)
                    .sent(true)
                    .build());
        } catch (Exception ex) {
            try {
                emailNotificationRepository.save(EmailNotification.builder()
                        .recipient(recipient)
                        .subject(subject)
                        .body(body)
                        .sent(false)
                        .failureReason(ex.getMessage())
                        .build());
            } catch (Exception ignored) {
                // Email failures must not block booking operations.
            }
        }
    }

    public List<EmailNotificationDto> getAll() {
        return emailNotificationRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private EmailNotificationDto mapToDto(EmailNotification email) {
        EmailNotificationDto dto = new EmailNotificationDto();
        dto.setId(email.getId());
        dto.setRecipient(email.getRecipient());
        dto.setSubject(email.getSubject());
        dto.setBody(email.getBody());
        dto.setSent(email.isSent());
        dto.setFailureReason(email.getFailureReason());
        dto.setCreatedAt(email.getCreatedAt());
        return dto;
    }
}

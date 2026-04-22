package com.smart_campus_hub.smart_campus_api.repository;

import com.smart_campus_hub.smart_campus_api.model.EmailNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailNotificationRepository extends JpaRepository<EmailNotification, Long> {
}

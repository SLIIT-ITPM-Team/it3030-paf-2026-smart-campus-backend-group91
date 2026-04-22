package com.smart_campus_hub.smart_campus_api.repository;

import com.smart_campus_hub.smart_campus_api.entity.AvailabilityWindow; // Import kala
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AvailabilityWindowRepository extends JpaRepository<AvailabilityWindow, Long> {
    List<AvailabilityWindow> findByResource_Id(Long resourceId);
}
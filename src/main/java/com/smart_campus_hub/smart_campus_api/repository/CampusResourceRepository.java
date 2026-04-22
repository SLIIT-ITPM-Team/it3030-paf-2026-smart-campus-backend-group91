package com.smart_campus_hub.smart_campus_api.repository;

import com.smart_campus_hub.smart_campus_api.model.CampusResource;
import com.smart_campus_hub.smart_campus_api.model.ResourceStatus;
import com.smart_campus_hub.smart_campus_api.model.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampusResourceRepository extends JpaRepository<CampusResource, Long> {
    List<CampusResource> findByStatus(ResourceStatus status);
    List<CampusResource> findByType(ResourceType type);
}

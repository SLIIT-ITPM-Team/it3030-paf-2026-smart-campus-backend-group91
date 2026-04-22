package com.smart_campus_hub.smart_campus_api.repository;

import com.smart_campus_hub.smart_campus_api.entity.Location; // Meka import karala thiyenne
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
}
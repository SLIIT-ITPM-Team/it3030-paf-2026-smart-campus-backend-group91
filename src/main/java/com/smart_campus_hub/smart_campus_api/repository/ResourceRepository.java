package com.smart_campus_hub.smart_campus_api.repository;

import com.smart_campus_hub.smart_campus_api.entity.Resource; // Import kala
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findByType(String type); 
    List<Resource> findByLocation_Id(Long locationId); 
    List<Resource> findByCapacityGreaterThanEqual(Integer capacity); 
    List<Resource> findByStatus(String status); 
}
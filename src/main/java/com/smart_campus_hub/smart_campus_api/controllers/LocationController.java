package com.smart_campus_hub.smart_campus_api.controllers;

import com.smart_campus_hub.smart_campus_api.entity.Location;
import com.smart_campus_hub.smart_campus_api.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin("*") 
public class LocationController {

    @Autowired
    private LocationRepository locationRepository;

    // Okkoma locations balanna (GET Request)
    @GetMapping
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    // Aluth location ekak database ekata danna (POST Request)
    @PostMapping
    public Location createLocation(@RequestBody Location location) {
        return locationRepository.save(location);
    }
}

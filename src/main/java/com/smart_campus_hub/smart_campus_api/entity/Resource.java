package com.smart_campus_hub.smart_campus_api.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type;
    private Integer capacity;
    private String status;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<AvailabilityWindow> availabilityWindows;

    public Resource() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public List<AvailabilityWindow> getAvailabilityWindows() { return availabilityWindows; }
    public void setAvailabilityWindows(List<AvailabilityWindow> availabilityWindows) { this.availabilityWindows = availabilityWindows; }
}
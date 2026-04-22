package com.smart_campus_hub.smart_campus_api.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String buildingName;
    private String floorNumber;
    private String roomNumber;
    private String description;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Resource> resources;

    public Location() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }
    public String getFloorNumber() { return floorNumber; }
    public void setFloorNumber(String floorNumber) { this.floorNumber = floorNumber; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<Resource> getResources() { return resources; }
    public void setResources(List<Resource> resources) { this.resources = resources; }
}
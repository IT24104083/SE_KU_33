package com.example.test002.model;

import java.math.BigDecimal;

public class Venue {
    private Integer venueID;
    private String name;
    private String location;
    private BigDecimal venueCost;
    private Integer capacity;
    private String availability;

    // Constructors
    public Venue() {}

    public Venue(String name, String location, BigDecimal venueCost, Integer capacity) {
        this.name = name;
        this.location = location;
        this.venueCost = venueCost;
        this.capacity = capacity;
        this.availability = "Available";
    }

    // Getters and Setters
    public Integer getVenueID() {
        return venueID;
    }

    public void setVenueID(Integer venueID) {
        this.venueID = venueID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BigDecimal getVenueCost() {
        return venueCost;
    }

    public void setVenueCost(BigDecimal venueCost) {
        this.venueCost = venueCost;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    @Override
    public String toString() {
        return "Venue{" +
                "venueID=" + venueID +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", venueCost=" + venueCost +
                ", capacity=" + capacity +
                ", availability='" + availability + '\'' +
                '}';
    }
}

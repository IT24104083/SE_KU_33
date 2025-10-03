package com.example.test002.service;

import com.example.test002.model.Venue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class VenueService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Get all venues
    public List<Venue> getAllVenues() {
        String sql = "SELECT * FROM Venue ORDER BY Name";
        return jdbcTemplate.query(sql, new VenueRowMapper());
    }

    // Get only available venues
    public List<Venue> getAvailableVenues() {
        String sql = "SELECT * FROM Venue WHERE Availability = 'Available' ORDER BY Name";
        return jdbcTemplate.query(sql, new VenueRowMapper());
    }

    // Get a single venue by ID
    public Venue getVenueById(Integer venueId) {
        String sql = "SELECT * FROM Venue WHERE VenueID = ?";
        return jdbcTemplate.queryForObject(sql, new VenueRowMapper(), venueId);
    }

    // Update venue availability (Booked / Available)
    public void updateVenueAvailability(Integer venueId, String availability) {
        String sql = "UPDATE Venue SET Availability = ? WHERE VenueID = ?";
        jdbcTemplate.update(sql, availability, venueId);
    }

    // Update venue details (Name, Location, VenueCost, Capacity)
    public void updateVenue(Integer venueId, String name, String location,
                            BigDecimal venueCost, Integer capacity) {
        String sql = "UPDATE Venue SET Name = ?, Location = ?, VenueCost = ?, Capacity = ? WHERE VenueID = ?";
        jdbcTemplate.update(sql, name, location, venueCost, capacity, venueId);
    }

    // Search venues with filters
    public List<Venue> searchVenues(String location, Double venueCost, Integer capacity) {
        StringBuilder sql = new StringBuilder("SELECT * FROM Venue WHERE Availability = 'Available'");
        List<Object> params = new ArrayList<>();

        if (location != null && !location.isEmpty()) {
            sql.append(" AND Location LIKE ?");
            params.add("%" + location + "%");
        }

        if (venueCost != null) {
            sql.append(" AND VenueCost = ?");
            params.add(venueCost);
        }

        if (capacity != null) {
            sql.append(" AND Capacity = ?");
            params.add(capacity);
        }

        sql.append(" ORDER BY Name");

        return jdbcTemplate.query(sql.toString(), new VenueRowMapper(), params.toArray());
    }

    // RowMapper for Venue model
    private static class VenueRowMapper implements RowMapper<Venue> {
        @Override
        public Venue mapRow(ResultSet rs, int rowNum) throws SQLException {
            Venue venue = new Venue();
            venue.setVenueID(rs.getInt("VenueID"));
            venue.setName(rs.getString("Name"));
            venue.setLocation(rs.getString("Location"));
            venue.setVenueCost(rs.getBigDecimal("VenueCost"));
            venue.setCapacity(rs.getInt("Capacity"));
            venue.setAvailability(rs.getString("Availability"));
            return venue;
        }
    }
}
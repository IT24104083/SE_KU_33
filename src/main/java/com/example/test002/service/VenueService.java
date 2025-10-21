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

    // Get only available venues (without date filter - for general listing)
    public List<Venue> getAvailableVenues() {
        String sql = "SELECT * FROM Venue WHERE Availability = 'Available' ORDER BY Name";
        return jdbcTemplate.query(sql, new VenueRowMapper());
    }

    // NEW: Get available venues for a specific date
    public List<Venue> getAvailableVenuesForDate(String eventDate) {
        String sql = "SELECT v.* FROM Venue v " +
                "WHERE v.Availability = 'Available' " +
                "AND v.VenueID NOT IN (" +
                "   SELECT va.VenueID FROM VenueAvailability va WHERE va.UnavailableDate = ?" +
                ") " +
                "ORDER BY v.Name";
        return jdbcTemplate.query(sql, new VenueRowMapper(), eventDate);
    }

    // Get a single venue by ID
    public Venue getVenueById(Integer venueId) {
        String sql = "SELECT * FROM Venue WHERE VenueID = ?";
        return jdbcTemplate.queryForObject(sql, new VenueRowMapper(), venueId);
    }

    // NEW: Check if venue is available for specific date
    public boolean isVenueAvailableForDate(Integer venueId, String eventDate) {
        try {
            String sql = "SELECT COUNT(*) FROM VenueAvailability WHERE VenueID = ? AND UnavailableDate = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, venueId, eventDate);
            return count == null || count == 0;
        } catch (Exception e) {
            return false;
        }
    }

    // UPDATED: Update venue availability - now date-specific
    public void updateVenueAvailability(Integer venueId, String eventDate, String action) {
        if ("book".equals(action)) {
            // Block venue for specific date
            String sql = "INSERT INTO VenueAvailability (VenueID, UnavailableDate, Reason) VALUES (?, ?, 'Event Booking')";
            jdbcTemplate.update(sql, venueId, eventDate);
        } else if ("release".equals(action)) {
            // Release venue for specific date
            String sql = "DELETE FROM VenueAvailability WHERE VenueID = ? AND UnavailableDate = ?";
            jdbcTemplate.update(sql, venueId, eventDate);
        }
    }

    // Update venue details (Name, Location, VenueCost, Capacity)
    public void updateVenue(Integer venueId, String name, String location,
                            BigDecimal venueCost, Integer capacity) {
        String sql = "UPDATE Venue SET Name = ?, Location = ?, VenueCost = ?, Capacity = ? WHERE VenueID = ?";
        jdbcTemplate.update(sql, name, location, venueCost, capacity, venueId);
    }

    // UPDATED: Search venues with date-based availability
    public List<Venue> searchVenues(String location, Double venueCost, Integer capacity, String eventDate) {
        StringBuilder sql = new StringBuilder(
                "SELECT v.* FROM Venue v " +
                        "WHERE v.Availability = 'Available' " +
                        "AND v.VenueID NOT IN (" +
                        "   SELECT va.VenueID FROM VenueAvailability va WHERE va.UnavailableDate = ?" +
                        ")"
        );
        List<Object> params = new ArrayList<>();
        params.add(eventDate);

        if (location != null && !location.isEmpty()) {
            sql.append(" AND v.Location LIKE ?");
            params.add("%" + location + "%");
        }

        if (venueCost != null) {
            sql.append(" AND v.VenueCost <= ?");
            params.add(venueCost);
        }

        if (capacity != null) {
            sql.append(" AND v.Capacity >= ?");
            params.add(capacity);
        }

        sql.append(" ORDER BY v.Name");

        return jdbcTemplate.query(sql.toString(), new VenueRowMapper(), params.toArray());
    }

    // NEW: Get venue's booked dates
    public List<String> getVenueBookedDates(Integer venueId) {
        String sql = "SELECT UnavailableDate FROM VenueAvailability WHERE VenueID = ? ORDER BY UnavailableDate";
        return jdbcTemplate.queryForList(sql, String.class, venueId);
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

    // Add this method to VenueService.java for backward compatibility
    public List<Venue> searchVenuesBasic(String location, Double venueCost, Integer capacity) {
        StringBuilder sql = new StringBuilder("SELECT * FROM Venue WHERE Availability = 'Available'");
        List<Object> params = new ArrayList<>();

        if (location != null && !location.isEmpty()) {
            sql.append(" AND Location LIKE ?");
            params.add("%" + location + "%");
        }

        if (venueCost != null) {
            sql.append(" AND VenueCost <= ?");
            params.add(venueCost);
        }

        if (capacity != null) {
            sql.append(" AND Capacity >= ?");
            params.add(capacity);
        }

        sql.append(" ORDER BY Name");

        return jdbcTemplate.query(sql.toString(), new VenueRowMapper(), params.toArray());
    }
}
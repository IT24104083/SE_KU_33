package com.example.test002.service;

import com.example.test002.model.Venue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class VenueService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Venue> getAllVenues() {
        String sql = "SELECT * FROM Venue ORDER BY Name";
        return jdbcTemplate.query(sql, new VenueRowMapper());
    }

    public List<Venue> getAvailableVenues() {
        String sql = "SELECT * FROM Venue WHERE Availability = 'Available' ORDER BY Name";
        return jdbcTemplate.query(sql, new VenueRowMapper());
    }

    public Venue getVenueById(Integer venueId) {
        String sql = "SELECT * FROM Venue WHERE VenueID = ?";
        return jdbcTemplate.queryForObject(sql, new VenueRowMapper(), venueId);
    }

    public void updateVenueAvailability(Integer venueId, String availability) {
        String sql = "UPDATE Venue SET Availability = ? WHERE VenueID = ?";
        jdbcTemplate.update(sql, availability, venueId);
    }

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
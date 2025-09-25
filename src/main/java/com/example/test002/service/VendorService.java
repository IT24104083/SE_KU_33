package com.example.test002.service;

import com.example.test002.model.Vendor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class VendorService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Vendor> getAllVendors() {
        String sql = "SELECT * FROM Vendor ORDER BY VendorType, VendorName";
        return jdbcTemplate.query(sql, new VendorRowMapper());
    }

    public List<Vendor> getVendorsByType(String vendorType) {
        String sql = "SELECT * FROM Vendor WHERE VendorType = ? AND Availability = 'Available' ORDER BY VendorName";
        return jdbcTemplate.query(sql, new VendorRowMapper(), vendorType);
    }

    public List<Vendor> getAvailableVendors() {
        String sql = "SELECT * FROM Vendor WHERE Availability = 'Available' ORDER BY VendorType, VendorName";
        return jdbcTemplate.query(sql, new VendorRowMapper());
    }

    public Vendor getVendorById(Integer vendorId) {
        String sql = "SELECT * FROM Vendor WHERE VendorID = ?";
        return jdbcTemplate.queryForObject(sql, new VendorRowMapper(), vendorId);
    }

    public void updateVendorAvailability(Integer vendorId, String availability) {
        String sql = "UPDATE Vendor SET Availability = ? WHERE VendorID = ?";
        jdbcTemplate.update(sql, availability, vendorId);
    }

    private static class VendorRowMapper implements RowMapper<Vendor> {
        @Override
        public Vendor mapRow(ResultSet rs, int rowNum) throws SQLException {
            Vendor vendor = new Vendor();
            vendor.setVendorID(rs.getInt("VendorID"));
            vendor.setVendorName(rs.getString("VendorName"));
            vendor.setVendorType(rs.getString("VendorType"));
            vendor.setPrice(rs.getBigDecimal("Price"));
            vendor.setContactNo(rs.getString("ContactNo"));
            vendor.setAvailability(rs.getString("Availability"));
            return vendor;
        }
    }
}
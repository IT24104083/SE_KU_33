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
        return jdbcTemplate.query("SELECT * FROM Vendor", new VendorRowMapper());
    }

    public Vendor getVendorById(Integer id) {
        return jdbcTemplate.queryForObject("SELECT * FROM Vendor WHERE VendorID=?", new VendorRowMapper(), id);
    }

    public void addVendor(Vendor vendor) {
        jdbcTemplate.update("INSERT INTO Vendor (VendorName, VendorType, Price, ContactNo, Availability) VALUES (?, ?, ?, ?, ?)",
                vendor.getVendorName(), vendor.getVendorType(), vendor.getPrice(), vendor.getContactNo(), vendor.getAvailability());
    }

    public void updateVendor(Vendor vendor) {
        jdbcTemplate.update("UPDATE Vendor SET VendorName=?, VendorType=?, Price=?, ContactNo=?, Availability=? WHERE VendorID=?",
                vendor.getVendorName(), vendor.getVendorType(), vendor.getPrice(), vendor.getContactNo(), vendor.getAvailability(), vendor.getVendorID());
    }

    public void deleteVendor(Integer id) {
        jdbcTemplate.update("DELETE FROM Vendor WHERE VendorID=?", id);
    }

    private static class VendorRowMapper implements RowMapper<Vendor> {
        public Vendor mapRow(ResultSet rs, int rowNum) throws SQLException {
            Vendor v = new Vendor();
            v.setVendorID(rs.getInt("VendorID"));
            v.setVendorName(rs.getString("VendorName"));
            v.setVendorType(rs.getString("VendorType"));
            v.setPrice(rs.getBigDecimal("Price"));
            v.setContactNo(rs.getString("ContactNo"));
            v.setAvailability(rs.getString("Availability"));
            return v;
        }
    }
}


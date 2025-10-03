package com.example.test002.service;

import com.example.test002.model.Inquiry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Service
public class InquiryService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CustomerService customerService;

    // Create a new inquiry
    public Inquiry createInquiry(Inquiry inquiry) {
        String sql = "INSERT INTO EventInquiry (Budget, ProposedDate, GuestCount, SpecialRequests, " +
                "CustomerID, Photographer, Catering, DJ, Decorations, EventType, Status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setBigDecimal(1, inquiry.getBudget());
            ps.setDate(2, java.sql.Date.valueOf(inquiry.getProposedDate()));
            ps.setInt(3, inquiry.getGuestCount());
            ps.setString(4, inquiry.getSpecialRequests());
            ps.setInt(5, inquiry.getCustomerID());
            ps.setBoolean(6, inquiry.getPhotographer());
            ps.setBoolean(7, inquiry.getCatering());
            ps.setBoolean(8, inquiry.getDj());
            ps.setBoolean(9, inquiry.getDecorations());
            ps.setString(10, inquiry.getEventType());
            ps.setString(11, inquiry.getStatus());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            inquiry.setInquiryID(keyHolder.getKey().intValue());
        }

        return inquiry;
    }

    // Get all inquiries
    public List<Inquiry> getAllInquiries() {
        String sql = "SELECT * FROM EventInquiry ORDER BY InquiryID ASC"; // ✅ Fetch all columns
        return jdbcTemplate.query(sql, new InquiryRowMapper());
    }

    // Get inquiries by customer ID
    public List<Inquiry> getInquiriesByCustomerId(Integer customerId) {
        String sql = "SELECT * FROM EventInquiry WHERE CustomerID = ? ORDER BY InquiryID DESC";
        return jdbcTemplate.query(sql, new InquiryRowMapper(), customerId);
    }

    // Get inquiry by ID
    public Inquiry getInquiryById(Integer id) {
        String sql = "SELECT * FROM EventInquiry WHERE InquiryID = ?";
        return jdbcTemplate.queryForObject(sql, new InquiryRowMapper(), id);
    }

    // Get pending inquiries
    public List<Inquiry> getPendingInquiries() {
        String sql = "SELECT * FROM EventInquiry WHERE Status = 'Pending' ORDER BY InquiryID DESC";
        return jdbcTemplate.query(sql, new InquiryRowMapper());
    }

    // RowMapper for Inquiry
    private static class InquiryRowMapper implements RowMapper<Inquiry> {
        @Override
        public Inquiry mapRow(ResultSet rs, int rowNum) throws SQLException {
            Inquiry inquiry = new Inquiry();
            inquiry.setInquiryID(rs.getInt("InquiryID"));
            inquiry.setBudget(rs.getBigDecimal("Budget"));
            inquiry.setProposedDate(rs.getDate("ProposedDate").toLocalDate());
            inquiry.setGuestCount(rs.getInt("GuestCount"));
            inquiry.setSpecialRequests(rs.getString("SpecialRequests"));
            inquiry.setCustomerID(rs.getInt("CustomerID"));
            inquiry.setPhotographer(rs.getBoolean("Photographer"));
            inquiry.setCatering(rs.getBoolean("Catering"));
            inquiry.setDj(rs.getBoolean("DJ"));
            inquiry.setDecorations(rs.getBoolean("Decorations"));
            inquiry.setEventType(rs.getString("EventType"));
            inquiry.setStatus(rs.getString("Status"));
            return inquiry;
        }
    }

    // Get inquiries count
    public int getInquiriesCount() {
        String sql = "SELECT COUNT(*) FROM EventInquiry";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    // Get pending inquiries count
    public int getPendingInquiriesCount() {
        String sql = "SELECT COUNT(*) FROM EventInquiry WHERE Status = 'Pending'";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }
}
package com.example.test002.service;

import com.example.test002.model.Inquiry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

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

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setBigDecimal(1, inquiry.getBudget());
                ps.setDate(2, java.sql.Date.valueOf(inquiry.getProposedDate()));
                ps.setInt(3, inquiry.getGuestCount());
                ps.setString(4, inquiry.getSpecialRequests());
                ps.setInt(5, inquiry.getCustomerID());
                ps.setBoolean(6, inquiry.getPhotographer() != null ? inquiry.getPhotographer() : false);
                ps.setBoolean(7, inquiry.getCatering() != null ? inquiry.getCatering() : false);
                ps.setBoolean(8, inquiry.getDj() != null ? inquiry.getDj() : false);
                ps.setBoolean(9, inquiry.getDecorations() != null ? inquiry.getDecorations() : false);
                ps.setString(10, inquiry.getEventType());
                ps.setString(11, inquiry.getStatus() != null ? inquiry.getStatus() : "Pending");
                return ps;
            }, keyHolder);

            if (keyHolder.getKey() != null) {
                inquiry.setInquiryID(keyHolder.getKey().intValue());
                System.out.println("DEBUG: Created inquiry with ID: " + inquiry.getInquiryID());
            }

            return inquiry;
        } catch (Exception e) {
            System.err.println("ERROR: Failed to create inquiry: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create inquiry: " + e.getMessage(), e);
        }
    }

    // Get all inquiries with proper error handling
    public List<Inquiry> getAllInquiries() {
        try {
            String sql = "SELECT * FROM EventInquiry ORDER BY InquiryID DESC";
            List<Inquiry> inquiries = jdbcTemplate.query(sql, new InquiryRowMapper());

            System.out.println("DEBUG: getAllInquiries() - Found " + inquiries.size() + " inquiries");
            for (Inquiry inquiry : inquiries) {
                System.out.println("DEBUG: Inquiry ID: " + inquiry.getInquiryID() +
                        ", CustomerID: " + inquiry.getCustomerID() +
                        ", Status: " + inquiry.getStatus());
            }

            return inquiries;
        } catch (Exception e) {
            System.err.println("ERROR: Failed to fetch all inquiries: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // Return empty list instead of null
        }
    }

    // Get inquiries by customer ID
    public List<Inquiry> getInquiriesByCustomerId(Integer customerId) {
        try {
            String sql = "SELECT * FROM EventInquiry WHERE CustomerID = ? ORDER BY InquiryID DESC";
            List<Inquiry> inquiries = jdbcTemplate.query(sql, new InquiryRowMapper(), customerId);
            System.out.println("DEBUG: getInquiriesByCustomerId(" + customerId + ") - Found " + inquiries.size() + " inquiries");
            return inquiries;
        } catch (Exception e) {
            System.err.println("ERROR: Failed to fetch inquiries for customer " + customerId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Get inquiry by ID
    public Inquiry getInquiryById(Integer id) {
        try {
            String sql = "SELECT * FROM EventInquiry WHERE InquiryID = ?";
            Inquiry inquiry = jdbcTemplate.queryForObject(sql, new InquiryRowMapper(), id);
            System.out.println("DEBUG: getInquiryById(" + id + ") - Found inquiry: " + inquiry);
            return inquiry;
        } catch (Exception e) {
            System.err.println("ERROR: Inquiry not found with ID " + id + ": " + e.getMessage());
            throw new RuntimeException("Inquiry not found with ID: " + id);
        }
    }

    // Get pending inquiries
    public List<Inquiry> getPendingInquiries() {
        try {
            String sql = "SELECT * FROM EventInquiry WHERE Status = 'Pending' ORDER BY InquiryID DESC";
            List<Inquiry> inquiries = jdbcTemplate.query(sql, new InquiryRowMapper());
            System.out.println("DEBUG: getPendingInquiries() - Found " + inquiries.size() + " pending inquiries");
            return inquiries;
        } catch (Exception e) {
            System.err.println("ERROR: Failed to fetch pending inquiries: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Update inquiry status
    public void updateInquiryStatus(Integer inquiryId, String status) {
        try {
            String sql = "UPDATE EventInquiry SET Status = ? WHERE InquiryID = ?";
            int rowsUpdated = jdbcTemplate.update(sql, status, inquiryId);
            System.out.println("DEBUG: updateInquiryStatus(" + inquiryId + ", " + status + ") - Updated " + rowsUpdated + " rows");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to update inquiry status: " + e.getMessage());
            throw new RuntimeException("Failed to update inquiry status: " + e.getMessage(), e);
        }
    }

    // Delete inquiry
    public void deleteInquiry(Integer inquiryId) {
        try {
            String sql = "DELETE FROM EventInquiry WHERE InquiryID = ?";
            int rowsDeleted = jdbcTemplate.update(sql, inquiryId);
            System.out.println("DEBUG: deleteInquiry(" + inquiryId + ") - Deleted " + rowsDeleted + " rows");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to delete inquiry: " + e.getMessage());
            throw new RuntimeException("Failed to delete inquiry: " + e.getMessage(), e);
        }
    }

    // Get inquiries count
    public int getInquiriesCount() {
        try {
            String sql = "SELECT COUNT(*) FROM EventInquiry";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            System.out.println("DEBUG: getInquiriesCount() - Total inquiries: " + count);
            return count != null ? count : 0;
        } catch (Exception e) {
            System.err.println("ERROR: Failed to get inquiries count: " + e.getMessage());
            return 0;
        }
    }

    // Get pending inquiries count
    public int getPendingInquiriesCount() {
        try {
            String sql = "SELECT COUNT(*) FROM EventInquiry WHERE Status = 'Pending'";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            System.out.println("DEBUG: getPendingInquiriesCount() - Pending inquiries: " + count);
            return count != null ? count : 0;
        } catch (Exception e) {
            System.err.println("ERROR: Failed to get pending inquiries count: " + e.getMessage());
            return 0;
        }
    }

    // Get approved inquiries count
    public int getApprovedInquiriesCount() {
        try {
            String sql = "SELECT COUNT(*) FROM EventInquiry WHERE Status = 'Approved'";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            System.err.println("ERROR: Failed to get approved inquiries count: " + e.getMessage());
            return 0;
        }
    }

    // Get rejected inquiries count
    public int getRejectedInquiriesCount() {
        try {
            String sql = "SELECT COUNT(*) FROM EventInquiry WHERE Status = 'Rejected'";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            System.err.println("ERROR: Failed to get rejected inquiries count: " + e.getMessage());
            return 0;
        }
    }

    // Search inquiries by event type
    public List<Inquiry> searchInquiriesByEventType(String eventType) {
        try {
            String sql = "SELECT * FROM EventInquiry WHERE EventType LIKE ? ORDER BY InquiryID DESC";
            List<Inquiry> inquiries = jdbcTemplate.query(sql, new InquiryRowMapper(), "%" + eventType + "%");
            System.out.println("DEBUG: searchInquiriesByEventType(" + eventType + ") - Found " + inquiries.size() + " inquiries");
            return inquiries;
        } catch (Exception e) {
            System.err.println("ERROR: Failed to search inquiries by event type: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Get inquiries with customer details (for reports)
    public List<Inquiry> getInquiriesWithCustomerDetails() {
        try {
            String sql = "SELECT ei.*, c.Customer_Name, c.Email, c.Phone " +
                    "FROM EventInquiry ei " +
                    "JOIN Customer c ON ei.CustomerID = c.CustomerID " +
                    "ORDER BY ei.InquiryID DESC";

            List<Inquiry> inquiries = jdbcTemplate.query(sql, new InquiryWithCustomerRowMapper());
            System.out.println("DEBUG: getInquiriesWithCustomerDetails() - Found " + inquiries.size() + " inquiries with customer details");
            return inquiries;
        } catch (Exception e) {
            System.err.println("ERROR: Failed to fetch inquiries with customer details: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // RowMapper for Inquiry with null-safe handling
    private static class InquiryRowMapper implements RowMapper<Inquiry> {
        @Override
        public Inquiry mapRow(ResultSet rs, int rowNum) throws SQLException {
            Inquiry inquiry = new Inquiry();

            try {
                inquiry.setInquiryID(rs.getInt("InquiryID"));

                // Handle null values for BigDecimal
                BigDecimal budget = rs.getBigDecimal("Budget");
                inquiry.setBudget(budget != null ? budget : BigDecimal.ZERO);

                // Handle date with null check
                java.sql.Date proposedDate = rs.getDate("ProposedDate");
                inquiry.setProposedDate(proposedDate != null ? proposedDate.toLocalDate() : null);

                inquiry.setGuestCount(rs.getInt("GuestCount"));
                inquiry.setSpecialRequests(rs.getString("SpecialRequests"));
                inquiry.setCustomerID(rs.getInt("CustomerID"));

                // Handle boolean fields with null checks
                inquiry.setPhotographer(rs.getBoolean("Photographer"));
                inquiry.setCatering(rs.getBoolean("Catering"));
                inquiry.setDj(rs.getBoolean("DJ"));
                inquiry.setDecorations(rs.getBoolean("Decorations"));

                inquiry.setEventType(rs.getString("EventType"));
                inquiry.setStatus(rs.getString("Status"));

            } catch (SQLException e) {
                System.err.println("ERROR: Failed to map row for inquiry: " + e.getMessage());
                throw e;
            }

            return inquiry;
        }
    }

    // RowMapper for Inquiry with Customer details
    private static class InquiryWithCustomerRowMapper implements RowMapper<Inquiry> {
        @Override
        public Inquiry mapRow(ResultSet rs, int rowNum) throws SQLException {
            Inquiry inquiry = new Inquiry();

            try {
                inquiry.setInquiryID(rs.getInt("InquiryID"));

                // Handle null values for BigDecimal
                BigDecimal budget = rs.getBigDecimal("Budget");
                inquiry.setBudget(budget != null ? budget : BigDecimal.ZERO);

                // Handle date with null check
                java.sql.Date proposedDate = rs.getDate("ProposedDate");
                inquiry.setProposedDate(proposedDate != null ? proposedDate.toLocalDate() : null);

                inquiry.setGuestCount(rs.getInt("GuestCount"));
                inquiry.setSpecialRequests(rs.getString("SpecialRequests"));
                inquiry.setCustomerID(rs.getInt("CustomerID"));

                // Handle boolean fields with null checks
                inquiry.setPhotographer(rs.getBoolean("Photographer"));
                inquiry.setCatering(rs.getBoolean("Catering"));
                inquiry.setDj(rs.getBoolean("DJ"));
                inquiry.setDecorations(rs.getBoolean("Decorations"));

                inquiry.setEventType(rs.getString("EventType"));
                inquiry.setStatus(rs.getString("Status"));

                // Note: Customer details are not stored in Inquiry model, but available in result set
                // You might want to create a DTO for this if needed

            } catch (SQLException e) {
                System.err.println("ERROR: Failed to map row for inquiry with customer details: " + e.getMessage());
                throw e;
            }

            return inquiry;
        }
    }

    // Debug method to check database connection and table status
    public void debugDatabaseStatus() {
        try {
            // Check if table exists and has data
            String checkTableSql = "SELECT COUNT(*) as table_count FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'EVENTINQUIRY'";
            Integer tableCount = jdbcTemplate.queryForObject(checkTableSql, Integer.class);
            System.out.println("DEBUG: EventInquiry table exists: " + (tableCount != null && tableCount > 0));

            // Check row count
            String countSql = "SELECT COUNT(*) FROM EventInquiry";
            Integer rowCount = jdbcTemplate.queryForObject(countSql, Integer.class);
            System.out.println("DEBUG: EventInquiry row count: " + rowCount);

            // Check sample data
            String sampleSql = "SELECT TOP 5 InquiryID, CustomerID, Status FROM EventInquiry ORDER BY InquiryID DESC";
            List<Map<String, Object>> sampleData = jdbcTemplate.queryForList(sampleSql);
            System.out.println("DEBUG: Sample data from EventInquiry:");
            for (Map<String, Object> row : sampleData) {
                System.out.println("  InquiryID: " + row.get("InquiryID") +
                        ", CustomerID: " + row.get("CustomerID") +
                        ", Status: " + row.get("Status"));
            }

        } catch (Exception e) {
            System.err.println("ERROR: Database debug failed: " + e.getMessage());
        }
    }

    // Add this method to your existing InquiryService class

    public void updateInquiry(Inquiry inquiry) {
        String sql = """
        UPDATE EventInquiry 
        SET Budget = ?, ProposedDate = ?, GuestCount = ?, SpecialRequests = ?,
            CustomerID = ?, Photographer = ?, Catering = ?, DJ = ?, Decorations = ?,
            EventType = ?, Status = ?
        WHERE InquiryID = ?
        """;

        int affected = jdbcTemplate.update(sql,
                inquiry.getBudget(),
                java.sql.Date.valueOf(inquiry.getProposedDate()),
                inquiry.getGuestCount(),
                inquiry.getSpecialRequests(),
                inquiry.getCustomerID(),
                inquiry.getPhotographer(),
                inquiry.getCatering(),
                inquiry.getDj(),
                inquiry.getDecorations(),
                inquiry.getEventType(),
                inquiry.getStatus(),
                inquiry.getInquiryID()
        );

        if (affected == 0) {
            throw new RuntimeException("Inquiry not found with ID: " + inquiry.getInquiryID());
        }
    }
}
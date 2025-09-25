package com.example.test002.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EventService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void createEvent(String eventType, String eventDate, int customerId, Integer inquiryId) {
        String sql = "INSERT INTO Event (EventType, EventDate, CustomerID, Status, InquiryID) " +
                "VALUES (?, ?, ?, 'Pending', ?)";
        jdbcTemplate.update(sql, eventType, eventDate, customerId, inquiryId);
    }

    public void updateInquiryStatus(Integer inquiryId, String status) {
        String sql = "UPDATE EventInquiry SET Status = ? WHERE InquiryID = ?";
        jdbcTemplate.update(sql, status, inquiryId);
    }

    public List<Map<String, Object>> getCreatedEvents() {
        String sql = "SELECT e.EventID, e.EventType, e.EventDate, e.Status, " +
                "c.Customer_Name, c.Email AS CustomerEmail, e.InquiryID " +
                "FROM Event e " +
                "JOIN Customer c ON e.CustomerID = c.CustomerID " +
                "ORDER BY e.EventID DESC";
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getRejectedInquiries() {
        String sql = "SELECT ei.*, c.Customer_Name " +
                "FROM EventInquiry ei " +
                "JOIN Customer c ON ei.CustomerID = c.CustomerID " +
                "WHERE ei.Status = 'Rejected' " +
                "ORDER BY ei.InquiryID DESC";
        return jdbcTemplate.queryForList(sql);
    }

    public Map<String, Object> getInquiryDetails(Integer inquiryId) {
        String sql = "SELECT ei.InquiryID, ei.Budget, ei.ProposedDate, ei.GuestCount, " +
                "ei.SpecialRequests, ei.CustomerID, ei.Photographer, ei.Catering, " +
                "ei.DJ, ei.Decorations, ei.EventType, ei.Status, " +
                "c.Customer_Name, c.Phone, c.Email " +
                "FROM EventInquiry ei " +
                "JOIN Customer c ON ei.CustomerID = c.CustomerID " +
                "WHERE ei.InquiryID = ?";
        return jdbcTemplate.queryForMap(sql, inquiryId);
    }

    public boolean eventExistsForInquiry(Integer inquiryId) {
        String sql = "SELECT COUNT(*) FROM Event WHERE InquiryID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, inquiryId);
        return count != null && count > 0;
    }

    public Map<String, Object> getEventById(Integer eventId) {
        String sql = "SELECT e.*, c.Customer_Name, c.Email, c.Phone, " +
                "v.Name as VenueName, v.Location, v.VenueCost " +
                "FROM Event e " +
                "JOIN Customer c ON e.CustomerID = c.CustomerID " +
                "LEFT JOIN Venue v ON e.VenueID = v.VenueID " +
                "WHERE e.EventID = ?";
        return jdbcTemplate.queryForMap(sql, eventId);
    }

    public void assignVenueToEvent(Integer eventId, Integer venueId) {
        // First check if there's already a venue assigned to this event
        String checkSql = "SELECT VenueID FROM Event WHERE EventID = ?";
        Integer currentVenueId = jdbcTemplate.queryForObject(checkSql, Integer.class, eventId);

        if (currentVenueId != null) {
            // Set old venue to available
            jdbcTemplate.update("UPDATE Venue SET Availability = 'Available' WHERE VenueID = ?", currentVenueId);
        }

        // Update the event with the new venue
        String updateSql = "UPDATE Event SET VenueID = ? WHERE EventID = ?";
        jdbcTemplate.update(updateSql, venueId, eventId);

        // Set new venue to booked
        jdbcTemplate.update("UPDATE Venue SET Availability = 'Booked' WHERE VenueID = ?", venueId);
    }

    public void assignVendorToEvent(Integer eventId, Integer vendorId, String serviceType) {
        // First check if there's already a vendor assigned for this service type
        String checkSql = "SELECT COUNT(*) FROM EventVendor WHERE EventID = ? AND ServiceType = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, eventId, serviceType);

        if (count != null && count > 0) {
            // Update existing record - first get the current vendor ID
            String getCurrentVendorSql = "SELECT VendorID FROM EventVendor WHERE EventID = ? AND ServiceType = ?";
            Integer currentVendorId = jdbcTemplate.queryForObject(getCurrentVendorSql, Integer.class, eventId, serviceType);

            // Update the vendor assignment
            String updateSql = "UPDATE EventVendor SET VendorID = ? WHERE EventID = ? AND ServiceType = ?";
            jdbcTemplate.update(updateSql, vendorId, eventId, serviceType);

            // Set old vendor to available
            if (currentVendorId != null) {
                jdbcTemplate.update("UPDATE Vendor SET Availability = 'Available' WHERE VendorID = ?", currentVendorId);
            }
        } else {
            // Insert new record
            String insertSql = "INSERT INTO EventVendor (EventID, VendorID, ServiceType) VALUES (?, ?, ?)";
            jdbcTemplate.update(insertSql, eventId, vendorId, serviceType);
        }

        // Set new vendor to booked
        jdbcTemplate.update("UPDATE Vendor SET Availability = 'Booked' WHERE VendorID = ?", vendorId);
    }

    public void removeVenueFromEvent(Integer eventId) {
        try {
            // Get current venue ID first
            String getSql = "SELECT VenueID FROM Event WHERE EventID = ?";
            Integer venueId = jdbcTemplate.queryForObject(getSql, Integer.class, eventId);

            if (venueId != null) {
                String sql = "UPDATE Event SET VenueID = NULL WHERE EventID = ?";
                jdbcTemplate.update(sql, eventId);

                // Update venue availability back to available
                sql = "UPDATE Venue SET Availability = 'Available' WHERE VenueID = ?";
                jdbcTemplate.update(sql, venueId);
            }
        } catch (Exception e) {
            // No venue found for this event, nothing to remove
            System.out.println("No venue found for event " + eventId);
        }
    }

    public void removeVendorFromEvent(Integer eventId, String serviceType) {
        try {
            // Get vendor ID first
            String getSql = "SELECT VendorID FROM EventVendor WHERE EventID = ? AND ServiceType = ?";
            Integer vendorId = jdbcTemplate.queryForObject(getSql, Integer.class, eventId, serviceType);

            if (vendorId != null) {
                String sql = "DELETE FROM EventVendor WHERE EventID = ? AND ServiceType = ?";
                jdbcTemplate.update(sql, eventId, serviceType);

                // Update vendor availability back to available
                sql = "UPDATE Vendor SET Availability = 'Available' WHERE VendorID = ?";
                jdbcTemplate.update(sql, vendorId);
            }
        } catch (Exception e) {
            // No vendor found for this service type, nothing to remove
            System.out.println("No vendor found for event " + eventId + " and service " + serviceType);
        }
    }

    public List<Map<String, Object>> getEventsNeedingVendors() {
        String sql = "SELECT e.EventID, e.EventType, e.EventDate, c.Customer_Name, " +
                "ei.Photographer, ei.Catering, ei.DJ, ei.Decorations, " +
                "e.VenueID, " +
                "(SELECT COUNT(*) FROM EventVendor WHERE EventID = e.EventID) as AssignedVendors " +
                "FROM Event e " +
                "JOIN Customer c ON e.CustomerID = c.CustomerID " +
                "JOIN EventInquiry ei ON e.InquiryID = ei.InquiryID " +
                "WHERE e.Status = 'Pending' " +
                "ORDER by e.EventDate";
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getInquiryDetailsForEvent(Integer eventId) {
        String sql = "SELECT ei.* FROM Event e " +
                "JOIN EventInquiry ei ON e.InquiryID = ei.InquiryID " +
                "WHERE e.EventID = ?";
        return jdbcTemplate.queryForList(sql, eventId);
    }

    // Get vendors assigned to an event
    public List<Map<String, Object>> getAssignedVendorsForEvent(Integer eventId) {
        String sql = "SELECT ev.*, v.VendorName, v.VendorType, v.Price, v.ContactNo, v.Availability " +
                "FROM EventVendor ev " +
                "JOIN Vendor v ON ev.VendorID = v.VendorID " +
                "WHERE ev.EventID = ?";
        return jdbcTemplate.queryForList(sql, eventId);
    }

    // Check if a service is already assigned to an event
    public boolean isServiceAssigned(Integer eventId, String serviceType) {
        try {
            String sql = "SELECT COUNT(*) FROM EventVendor WHERE EventID = ? AND ServiceType = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, eventId, serviceType);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // Update event status
    public void updateEventStatus(Integer eventId, String status) {
        String sql = "UPDATE Event SET Status = ? WHERE EventID = ?";
        jdbcTemplate.update(sql, status, eventId);
    }

    // Get events by status
    public List<Map<String, Object>> getEventsByStatus(String status) {
        String sql = "SELECT e.*, c.Customer_Name, c.Email, c.Phone " +
                "FROM Event e " +
                "JOIN Customer c ON e.CustomerID = c.CustomerID " +
                "WHERE e.Status = ? " +
                "ORDER BY e.EventDate";
        return jdbcTemplate.queryForList(sql, status);
    }

    // Get event count by status
    public int getEventCountByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM Event WHERE Status = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, status);
    }

    // Get all events with vendor and venue details
    public List<Map<String, Object>> getAllEventsWithDetails() {
        String sql = "SELECT e.*, c.Customer_Name, c.Email, c.Phone, " +
                "v.Name as VenueName, v.Location, v.VenueCost, " +
                "(SELECT COUNT(*) FROM EventVendor WHERE EventID = e.EventID) as VendorCount " +
                "FROM Event e " +
                "JOIN Customer c ON e.CustomerID = c.CustomerID " +
                "LEFT JOIN Venue v ON e.VenueID = v.VenueID " +
                "ORDER BY e.EventDate DESC";
        return jdbcTemplate.queryForList(sql);
    }

    // Check if venue is assigned to event
    public boolean isVenueAssigned(Integer eventId) {
        String sql = "SELECT VenueID FROM Event WHERE EventID = ?";
        Integer venueId = jdbcTemplate.queryForObject(sql, Integer.class, eventId);
        return venueId != null;
    }

    // Get event summary statistics
    public Map<String, Object> getEventStatistics() {
        String sql = "SELECT " +
                "COUNT(*) as totalEvents, " +
                "SUM(CASE WHEN Status = 'Pending' THEN 1 ELSE 0 END) as pendingEvents, " +
                "SUM(CASE WHEN Status = 'Confirmed' THEN 1 ELSE 0 END) as confirmedEvents, " +
                "SUM(CASE WHEN Status = 'Completed' THEN 1 ELSE 0 END) as completedEvents, " +
                "SUM(CASE WHEN VenueID IS NOT NULL THEN 1 ELSE 0 END) as eventsWithVenue, " +
                "SUM(CASE WHEN VenueID IS NULL THEN 1 ELSE 0 END) as eventsWithoutVenue " +
                "FROM Event";
        return jdbcTemplate.queryForMap(sql);
    }

    // Add these methods to your EventService class

    public List<Map<String, Object>> getAvailableVenuesWithFilter(String sortOrder, Double minPrice, Double maxPrice) {
        String sql = "SELECT * FROM Venue WHERE Availability = 'Available'";

        // Add price filters if provided
        if (minPrice != null) {
            sql += " AND VenueCost >= " + minPrice;
        }
        if (maxPrice != null) {
            sql += " AND VenueCost <= " + maxPrice;
        }

        // Add sorting
        if ("min-max".equals(sortOrder)) {
            sql += " ORDER BY VenueCost ASC";
        } else if ("max-min".equals(sortOrder)) {
            sql += " ORDER BY VenueCost DESC";
        } else {
            sql += " ORDER BY Name";
        }

        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getVendorsByTypeWithFilter(String vendorType, String sortOrder, Double minPrice, Double maxPrice) {
        String sql = "SELECT * FROM Vendor WHERE VendorType = ? AND Availability = 'Available'";

        // Add price filters if provided
        if (minPrice != null) {
            sql += " AND Price >= " + minPrice;
        }
        if (maxPrice != null) {
            sql += " AND Price <= " + maxPrice;
        }

        // Add sorting
        if ("min-max".equals(sortOrder)) {
            sql += " ORDER BY Price ASC";
        } else if ("max-min".equals(sortOrder)) {
            sql += " ORDER BY Price DESC";
        } else {
            sql += " ORDER BY VendorName";
        }

        return jdbcTemplate.queryForList(sql, vendorType);
    }

}
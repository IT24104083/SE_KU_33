package com.example.test002.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    // UPDATED: Date-based venue assignment
    public void assignVenueToEvent(Integer eventId, Integer venueId) {
        try {
            // First get the event date
            String eventDateSql = "SELECT EventDate FROM Event WHERE EventID = ?";
            String eventDate = jdbcTemplate.queryForObject(eventDateSql, String.class, eventId);

            // Check if venue is already booked on this date
            String availabilityCheckSql = "SELECT COUNT(*) FROM VenueAvailability WHERE VenueID = ? AND UnavailableDate = ?";
            Integer conflictCount = jdbcTemplate.queryForObject(availabilityCheckSql, Integer.class, venueId, eventDate);

            if (conflictCount != null && conflictCount > 0) {
                throw new RuntimeException("Venue is already booked on " + eventDate);
            }

            // Check if there's already a venue assigned to this event
            String checkSql = "SELECT VenueID FROM Event WHERE EventID = ?";
            Integer currentVenueId = jdbcTemplate.queryForObject(checkSql, Integer.class, eventId);

            if (currentVenueId != null) {
                // Release the old venue for this event date
                releaseVenueFromEvent(currentVenueId, eventDate);
            }

            // Update the event with the new venue
            String updateSql = "UPDATE Event SET VenueID = ? WHERE EventID = ?";
            jdbcTemplate.update(updateSql, venueId, eventId);

            // Block the new venue for this event date
            String blockSql = "INSERT INTO VenueAvailability (VenueID, UnavailableDate, EventID, Reason) VALUES (?, ?, ?, 'Event Booking')";
            jdbcTemplate.update(blockSql, venueId, eventDate, eventId);

        } catch (Exception e) {
            throw new RuntimeException("Failed to assign venue: " + e.getMessage(), e);
        }
    }

    // UPDATED: Date-based vendor assignment
    public void assignVendorToEvent(Integer eventId, Integer vendorId, String serviceType) {
        try {
            // First get the event date
            String eventDateSql = "SELECT EventDate FROM Event WHERE EventID = ?";
            String eventDate = jdbcTemplate.queryForObject(eventDateSql, String.class, eventId);

            // Check if vendor is already booked on this date
            String availabilityCheckSql = "SELECT COUNT(*) FROM VendorAvailability WHERE VendorID = ? AND UnavailableDate = ?";
            Integer conflictCount = jdbcTemplate.queryForObject(availabilityCheckSql, Integer.class, vendorId, eventDate);

            if (conflictCount != null && conflictCount > 0) {
                throw new RuntimeException("Vendor is already booked on " + eventDate);
            }

            // Check if there's already a vendor assigned for this service type
            String checkSql = "SELECT COUNT(*) FROM EventVendor WHERE EventID = ? AND ServiceType = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, eventId, serviceType);

            if (count != null && count > 0) {
                // Update existing record - first get the current vendor ID
                String getCurrentVendorSql = "SELECT VendorID FROM EventVendor WHERE EventID = ? AND ServiceType = ?";
                Integer currentVendorId = jdbcTemplate.queryForObject(getCurrentVendorSql, Integer.class, eventId, serviceType);

                // Update the vendor assignment
                String updateSql = "UPDATE EventVendor SET VendorID = ? WHERE EventID = ? AND ServiceType = ?";
                jdbcTemplate.update(updateSql, vendorId, eventId, serviceType);

                // Remove old vendor's date block
                if (currentVendorId != null) {
                    jdbcTemplate.update("DELETE FROM VendorAvailability WHERE VendorID = ? AND UnavailableDate = ? AND EventID = ?",
                            currentVendorId, eventDate, eventId);
                }
            } else {
                // Insert new record
                String insertSql = "INSERT INTO EventVendor (EventID, VendorID, ServiceType) VALUES (?, ?, ?)";
                jdbcTemplate.update(insertSql, eventId, vendorId, serviceType);
            }

            // Block the vendor for this date
            String blockSql = "INSERT INTO VendorAvailability (VendorID, UnavailableDate, EventID, Reason) VALUES (?, ?, ?, 'Event Booking')";
            jdbcTemplate.update(blockSql, vendorId, eventDate, eventId);

        } catch (Exception e) {
            throw new RuntimeException("Failed to assign vendor: " + e.getMessage(), e);
        }
    }

    // UPDATED: Date-based venue removal
    public void removeVenueFromEvent(Integer eventId) {
        try {
            // Get current venue ID and event date first
            String getSql = "SELECT e.VenueID, e.EventDate FROM Event e WHERE e.EventID = ?";
            Map<String, Object> event = jdbcTemplate.queryForMap(getSql, eventId);

            Integer venueId = (Integer) event.get("VenueID");
            String eventDate = (String) event.get("EventDate");

            if (venueId != null) {
                // Remove venue from event
                String sql = "UPDATE Event SET VenueID = NULL WHERE EventID = ?";
                jdbcTemplate.update(sql, eventId);

                // Release the venue for this specific date
                releaseVenueFromEvent(venueId, eventDate);
            }
        } catch (Exception e) {
            // No venue found for this event, nothing to remove
            System.out.println("No venue found for event " + eventId);
        }
    }

    // UPDATED: Date-based vendor removal
    public void removeVendorFromEvent(Integer eventId, String serviceType) {
        try {
            // Get vendor ID and event date first
            String getSql = "SELECT ev.VendorID, e.EventDate FROM EventVendor ev " +
                    "JOIN Event e ON ev.EventID = e.EventID " +
                    "WHERE ev.EventID = ? AND ev.ServiceType = ?";
            Map<String, Object> assignment = jdbcTemplate.queryForMap(getSql, eventId, serviceType);

            Integer vendorId = (Integer) assignment.get("VendorID");
            String eventDate = (String) assignment.get("EventDate");

            if (vendorId != null) {
                // Remove the vendor assignment
                String sql = "DELETE FROM EventVendor WHERE EventID = ? AND ServiceType = ?";
                jdbcTemplate.update(sql, eventId, serviceType);

                // Remove the date block for this vendor
                sql = "DELETE FROM VendorAvailability WHERE VendorID = ? AND UnavailableDate = ? AND EventID = ?";
                jdbcTemplate.update(sql, vendorId, eventDate, eventId);
            }
        } catch (Exception e) {
            // No vendor found for this service type, nothing to remove
            System.out.println("No vendor found for event " + eventId + " and service " + serviceType);
        }
    }

    // NEW: Helper method to release venue for specific date
    private void releaseVenueFromEvent(Integer venueId, String eventDate) {
        try {
            String sql = "DELETE FROM VenueAvailability WHERE VenueID = ? AND UnavailableDate = ?";
            jdbcTemplate.update(sql, venueId, eventDate);
        } catch (Exception e) {
            System.out.println("Error releasing venue " + venueId + " for date " + eventDate);
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
        String sql = "SELECT ev.*, v.VendorName, v.VendorType, v.Price, v.ContactNo " +
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

    // UPDATED: Get available venues with date-based filtering
    public List<Map<String, Object>> getAvailableVenuesWithFilter(String sortOrder, Double minPrice, Double maxPrice, String eventDate) {
        StringBuilder sql = new StringBuilder(
                "SELECT v.* FROM Venue v " +
                        "WHERE v.Availability = 'Available' " +
                        "AND v.VenueID NOT IN (" +
                        "   SELECT va.VenueID FROM VenueAvailability va WHERE va.UnavailableDate = ?" +
                        ")"
        );

        List<Object> params = new ArrayList<>();
        params.add(eventDate);

        // Add price filters if provided
        if (minPrice != null) {
            sql.append(" AND v.VenueCost >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND v.VenueCost <= ?");
            params.add(maxPrice);
        }

        // Add sorting
        if ("min-max".equals(sortOrder)) {
            sql.append(" ORDER BY v.VenueCost ASC");
        } else if ("max-min".equals(sortOrder)) {
            sql.append(" ORDER BY v.VenueCost DESC");
        } else {
            sql.append(" ORDER BY v.Name");
        }

        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    // UPDATED: Get vendors by type with date-based availability check
    public List<Map<String, Object>> getVendorsByTypeWithFilter(String vendorType, String eventDate, String sortOrder, Double minPrice, Double maxPrice) {
        String sql = "SELECT v.* FROM Vendor v " +
                "WHERE v.VendorType = ? AND v.IsActive = 1 " +
                "AND v.VendorID NOT IN (" +
                "   SELECT VendorID FROM VendorAvailability WHERE UnavailableDate = ?" +
                ")";

        // Add price filters if provided
        if (minPrice != null) {
            sql += " AND v.Price >= " + minPrice;
        }
        if (maxPrice != null) {
            sql += " AND v.Price <= " + maxPrice;
        }

        // Add sorting
        if ("min-max".equals(sortOrder)) {
            sql += " ORDER BY v.Price ASC";
        } else if ("max-min".equals(sortOrder)) {
            sql += " ORDER BY v.Price DESC";
        } else {
            sql += " ORDER BY v.VendorName";
        }

        return jdbcTemplate.queryForList(sql, vendorType, eventDate);
    }

    // NEW: Check venue availability for a specific date
    public boolean isVenueAvailable(Integer venueId, String date) {
        try {
            String sql = "SELECT COUNT(*) FROM VenueAvailability WHERE VenueID = ? AND UnavailableDate = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, venueId, date);
            return count == null || count == 0;
        } catch (Exception e) {
            return false;
        }
    }

    // NEW: Check vendor availability for a specific date
    public boolean isVendorAvailable(Integer vendorId, String date) {
        try {
            String sql = "SELECT COUNT(*) FROM VendorAvailability WHERE VendorID = ? AND UnavailableDate = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, vendorId, date);
            return count == null || count == 0;
        } catch (Exception e) {
            return false;
        }
    }

    // NEW: Get venue's booked dates
    public List<String> getVenueBookedDates(Integer venueId) {
        String sql = "SELECT UnavailableDate FROM VenueAvailability WHERE VenueID = ? ORDER BY UnavailableDate";
        return jdbcTemplate.queryForList(sql, String.class, venueId);
    }

    // NEW: Get vendor's booked dates
    public List<String> getVendorBookedDates(Integer vendorId) {
        String sql = "SELECT UnavailableDate FROM VendorAvailability WHERE VendorID = ? ORDER BY UnavailableDate";
        return jdbcTemplate.queryForList(sql, String.class, vendorId);
    }

    // NEW: Get event date
    public String getEventDate(Integer eventId) {
        try {
            String sql = "SELECT EventDate FROM Event WHERE EventID = ?";
            return jdbcTemplate.queryForObject(sql, String.class, eventId);
        } catch (Exception e) {
            return null;
        }
    }
}
package com.example.test002.controller;

import com.example.test002.service.EventService;
import com.example.test002.service.VendorService;
import com.example.test002.service.VenueService;
import com.example.test002.config.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/event-coordinator/events")
public class EventManagementController {

    @Autowired
    private EventService eventService;

    @Autowired
    private VendorService vendorService;

    @Autowired
    private VenueService venueService;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private boolean isAuthorized() {
        Object userRole = sessionManager.getAttribute("userRole");
        return userRole != null && "EventCoordinator".equals(userRole.toString());
    }

    // Event Overview Page
    @GetMapping("/manage/{eventId}")
    public String manageEvent(@PathVariable("eventId") Integer eventId, Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        try {
            Map<String, Object> event = eventService.getEventById(eventId);
            List<Map<String, Object>> inquiryDetails = eventService.getInquiryDetailsForEvent(eventId);
            List<Map<String, Object>> assignedVendors = eventService.getAssignedVendorsForEvent(eventId);
            boolean venueAssigned = eventService.isVenueAssigned(eventId);

            model.addAttribute("event", event);
            model.addAttribute("inquiry", inquiryDetails.get(0));
            model.addAttribute("assignedVendors", assignedVendors);
            model.addAttribute("venueAssigned", venueAssigned);
            model.addAttribute("assignedVendorsCount", assignedVendors.size());

            return "event-coordinator/event-overview";
        } catch (Exception e) {
            return "redirect:/event-coordinator/dashboard?error=event_not_found";
        }
    }

    // UPDATED: Manage Venue Page with date-based filtering
    @GetMapping("/manage/{eventId}/venue")
    public String manageVenue(@PathVariable("eventId") Integer eventId,
                              @RequestParam(value = "sort", required = false) String sortOrder,
                              @RequestParam(value = "minPrice", required = false) Double minPrice,
                              @RequestParam(value = "maxPrice", required = false) Double maxPrice,
                              Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        try {
            Map<String, Object> event = eventService.getEventById(eventId);
            boolean venueAssigned = eventService.isVenueAssigned(eventId);
            Map<String, Object> currentVenue = null;

            // Get event date for venue availability checks
            String eventDate = eventService.getEventDate(eventId);
            if (eventDate == null) {
                throw new RuntimeException("Event date not found");
            }

            if (venueAssigned) {
                String sql = "SELECT v.* FROM Venue v JOIN Event e ON v.VenueID = e.VenueID WHERE e.EventID = ?";
                currentVenue = jdbcTemplate.queryForMap(sql, eventId);
            }

            // Get available venues with date-based filtering
            List<Map<String, Object>> availableVenues = eventService.getAvailableVenuesWithFilter(sortOrder, minPrice, maxPrice, eventDate);

            model.addAttribute("event", event);
            model.addAttribute("eventDate", eventDate); // Add event date to model
            model.addAttribute("venueAssigned", venueAssigned);
            model.addAttribute("currentVenue", currentVenue);
            model.addAttribute("availableVenues", availableVenues);
            model.addAttribute("currentSort", sortOrder);
            model.addAttribute("currentMinPrice", minPrice);
            model.addAttribute("currentMaxPrice", maxPrice);

            return "event-coordinator/manage-venue";
        } catch (Exception e) {
            return "redirect:/event-coordinator/events/manage/" + eventId + "?error=venue_error";
        }
    }

    // UPDATED: Manage Vendors Page with date-based filtering
    @GetMapping("/manage/{eventId}/vendors")
    public String manageVendors(@PathVariable("eventId") Integer eventId,
                                @RequestParam(value = "sort", required = false) String sortOrder,
                                @RequestParam(value = "minPrice", required = false) Double minPrice,
                                @RequestParam(value = "maxPrice", required = false) Double maxPrice,
                                Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        try {
            Map<String, Object> event = eventService.getEventById(eventId);
            List<Map<String, Object>> inquiryDetails = eventService.getInquiryDetailsForEvent(eventId);
            List<Map<String, Object>> assignedVendors = eventService.getAssignedVendorsForEvent(eventId);

            // Get event date for vendor availability checks
            String eventDate = eventService.getEventDate(eventId);
            if (eventDate == null) {
                throw new RuntimeException("Event date not found");
            }

            // Check which services are assigned
            boolean photographerAssigned = eventService.isServiceAssigned(eventId, "Photographer");
            boolean catererAssigned = eventService.isServiceAssigned(eventId, "Caterer");
            boolean djAssigned = eventService.isServiceAssigned(eventId, "DJ");
            boolean decoratorAssigned = eventService.isServiceAssigned(eventId, "Decorator");

            // Get vendors with date-based availability filtering
            List<Map<String, Object>> photographers = eventService.getVendorsByTypeWithFilter("Photographer", eventDate, sortOrder, minPrice, maxPrice);
            List<Map<String, Object>> caterers = eventService.getVendorsByTypeWithFilter("Caterer", eventDate, sortOrder, minPrice, maxPrice);
            List<Map<String, Object>> djs = eventService.getVendorsByTypeWithFilter("DJ", eventDate, sortOrder, minPrice, maxPrice);
            List<Map<String, Object>> decorators = eventService.getVendorsByTypeWithFilter("Decorator", eventDate, sortOrder, minPrice, maxPrice);

            model.addAttribute("event", event);
            model.addAttribute("eventDate", eventDate);
            model.addAttribute("inquiry", inquiryDetails.get(0));
            model.addAttribute("assignedVendors", assignedVendors);
            model.addAttribute("photographers", photographers);
            model.addAttribute("caterers", caterers);
            model.addAttribute("djs", djs);
            model.addAttribute("decorators", decorators);
            model.addAttribute("photographerAssigned", photographerAssigned);
            model.addAttribute("catererAssigned", catererAssigned);
            model.addAttribute("djAssigned", djAssigned);
            model.addAttribute("decoratorAssigned", decoratorAssigned);
            model.addAttribute("currentSort", sortOrder);
            model.addAttribute("currentMinPrice", minPrice);
            model.addAttribute("currentMaxPrice", maxPrice);

            return "event-coordinator/manage-vendors";
        } catch (Exception e) {
            return "redirect:/event-coordinator/events/manage/" + eventId + "?error=vendor_error";
        }
    }

    // UPDATED: Assign venue to event (uses date-based method)
    @PostMapping("/assign-venue")
    public String assignVenue(@RequestParam Integer eventId,
                              @RequestParam Integer venueId,
                              RedirectAttributes redirectAttributes) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        try {
            eventService.assignVenueToEvent(eventId, venueId);
            redirectAttributes.addFlashAttribute("success", "Venue assigned successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to assign venue: " + e.getMessage());
        }

        return "redirect:/event-coordinator/events/manage/" + eventId + "/venue";
    }

    // UPDATED: Assign vendor to event for specific service (date-based)
    @PostMapping("/assign-vendor")
    public String assignVendor(@RequestParam Integer eventId,
                               @RequestParam Integer vendorId,
                               @RequestParam String serviceType,
                               RedirectAttributes redirectAttributes) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        try {
            eventService.assignVendorToEvent(eventId, vendorId, serviceType);
            redirectAttributes.addFlashAttribute("success", serviceType + " assigned successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to assign " + serviceType + ": " + e.getMessage());
        }

        return "redirect:/event-coordinator/events/manage/" + eventId + "/vendors";
    }

    // UPDATED: Remove venue from event (uses date-based method)
    @PostMapping("/remove-venue")
    public String removeVenue(@RequestParam Integer eventId,
                              RedirectAttributes redirectAttributes) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        try {
            eventService.removeVenueFromEvent(eventId);
            redirectAttributes.addFlashAttribute("success", "Venue removed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to remove venue: " + e.getMessage());
        }

        return "redirect:/event-coordinator/events/manage/" + eventId + "/venue";
    }

    // UPDATED: Remove vendor service from event (date-based)
    @PostMapping("/remove-vendor-service")
    public String removeVendorService(@RequestParam Integer eventId,
                                      @RequestParam String serviceType,
                                      RedirectAttributes redirectAttributes) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        try {
            eventService.removeVendorFromEvent(eventId, serviceType);
            redirectAttributes.addFlashAttribute("success", serviceType + " service removed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to remove " + serviceType + " service: " + e.getMessage());
        }

        return "redirect:/event-coordinator/events/manage/" + eventId + "/vendors";
    }

    // View events needing vendor assignments
    @GetMapping("/needing-assignments")
    public String eventsNeedingAssignments(Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        List<Map<String, Object>> events = eventService.getEventsNeedingVendors();

        // Calculate needed services for each event
        int urgentCount = 0;
        int highPriorityCount = 0;
        int readyCount = 0;

        for (Map<String, Object> event : events) {
            Integer eventId = (Integer) event.get("EventID");
            Boolean photographerRequested = (Boolean) event.get("Photographer");
            Boolean cateringRequested = (Boolean) event.get("Catering");
            Boolean djRequested = (Boolean) event.get("DJ");
            Boolean decorationsRequested = (Boolean) event.get("Decorations");

            // Check which services are actually assigned
            boolean photographerAssigned = eventService.isServiceAssigned(eventId, "Photographer");
            boolean catererAssigned = eventService.isServiceAssigned(eventId, "Caterer");
            boolean djAssigned = eventService.isServiceAssigned(eventId, "DJ");
            boolean decoratorAssigned = eventService.isServiceAssigned(eventId, "Decorator");

            // Calculate needed services count
            int neededServices = 0;
            if (photographerRequested != null && photographerRequested && !photographerAssigned) neededServices++;
            if (cateringRequested != null && cateringRequested && !catererAssigned) neededServices++;
            if (djRequested != null && djRequested && !djAssigned) neededServices++;
            if (decorationsRequested != null && decorationsRequested && !decoratorAssigned) neededServices++;

            event.put("NeededServices", neededServices);
            event.put("PhotographerAssigned", photographerAssigned);
            event.put("CatererAssigned", catererAssigned);
            event.put("DJAssigned", djAssigned);
            event.put("DecoratorAssigned", decoratorAssigned);

            // Count priorities
            if (neededServices > 2) urgentCount++;
            else if (neededServices > 0) highPriorityCount++;
            else readyCount++;
        }

        model.addAttribute("events", events);
        model.addAttribute("urgentCount", urgentCount);
        model.addAttribute("highPriorityCount", highPriorityCount);
        model.addAttribute("readyCount", readyCount);
        model.addAttribute("pageTitle", "Events Needing Assignments");

        return "event-coordinator/events-needing-assignments";
    }

    // Confirm event (mark as completed)
    @PostMapping("/confirm-event")
    public String confirmEvent(@RequestParam Integer eventId,
                               RedirectAttributes redirectAttributes) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        try {
            eventService.updateEventStatus(eventId, "Confirmed");
            redirectAttributes.addFlashAttribute("success", "Event confirmed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to confirm event: " + e.getMessage());
        }

        return "redirect:/event-coordinator/events/manage/" + eventId;
    }

    // NEW: Check venue availability for a specific date (AJAX endpoint)
    @GetMapping("/check-venue-availability")
    @ResponseBody
    public Map<String, Object> checkVenueAvailability(@RequestParam Integer venueId,
                                                      @RequestParam String eventDate) {
        try {
            boolean isAvailable = eventService.isVenueAvailable(venueId, eventDate);
            return Map.of(
                    "available", isAvailable,
                    "message", isAvailable ? "Venue is available" : "Venue is already booked on this date"
            );
        } catch (Exception e) {
            return Map.of(
                    "available", false,
                    "message", "Error checking availability: " + e.getMessage()
            );
        }
    }

    // NEW: Check vendor availability for a specific date (AJAX endpoint)
    @GetMapping("/check-vendor-availability")
    @ResponseBody
    public Map<String, Object> checkVendorAvailability(@RequestParam Integer vendorId,
                                                       @RequestParam String eventDate) {
        try {
            boolean isAvailable = eventService.isVendorAvailable(vendorId, eventDate);
            return Map.of(
                    "available", isAvailable,
                    "message", isAvailable ? "Vendor is available" : "Vendor is already booked on this date"
            );
        } catch (Exception e) {
            return Map.of(
                    "available", false,
                    "message", "Error checking availability: " + e.getMessage()
            );
        }
    }

    // NEW: Get venue booked dates (AJAX endpoint)
    @GetMapping("/get-venue-booked-dates/{venueId}")
    @ResponseBody
    public List<String> getVenueBookedDates(@PathVariable Integer venueId) {
        try {
            return eventService.getVenueBookedDates(venueId);
        } catch (Exception e) {
            return List.of();
        }
    }

    // NEW: Get vendor booked dates (AJAX endpoint)
    @GetMapping("/get-vendor-booked-dates/{vendorId}")
    @ResponseBody
    public List<String> getVendorBookedDates(@PathVariable Integer vendorId) {
        try {
            return eventService.getVendorBookedDates(vendorId);
        } catch (Exception e) {
            return List.of();
        }
    }
}
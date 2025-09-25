package com.example.test002.controller;

import com.example.test002.model.Inquiry;
import com.example.test002.service.InquiryService;
import com.example.test002.service.EventService;
import com.example.test002.config.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/event-coordinator")
public class EventCoordinatorController {

    @Autowired
    private InquiryService inquiryService;

    @Autowired
    private EventService eventService;

    @Autowired
    private SessionManager sessionManager;

    // Check if user is authorized
    private boolean isAuthorized() {
        Object userRole = sessionManager.getAttribute("userRole");
        return userRole != null && "EventCoordinator".equals(userRole.toString());
    }

    // Show Event Coordinator dashboard
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        List<Inquiry> pendingInquiries = inquiryService.getPendingInquiries();
        List<Map<String, Object>> pendingEvents = eventService.getEventsByStatus("Pending");
        List<Map<String, Object>> confirmedEvents = eventService.getEventsByStatus("Confirmed");
        List<Map<String, Object>> rejectedInquiries = eventService.getRejectedInquiries();
        List<Map<String, Object>> eventsNeedingAssignments = eventService.getEventsNeedingVendors();

        model.addAttribute("pendingInquiries", pendingInquiries);
        model.addAttribute("pendingEvents", pendingEvents);
        model.addAttribute("confirmedEvents", confirmedEvents);
        model.addAttribute("rejectedInquiries", rejectedInquiries);
        model.addAttribute("pageTitle", "Event Coordinator Dashboard");
        model.addAttribute("pendingInquiriesCount", pendingInquiries.size());
        model.addAttribute("pendingEventsCount", pendingEvents.size());
        model.addAttribute("confirmedEventsCount", confirmedEvents.size());
        model.addAttribute("rejectedCount", rejectedInquiries.size());
        model.addAttribute("eventsNeedingAssignmentsCount", eventsNeedingAssignments.size());

        return "event-coordinator/event-coordinator-dashboard";
    }

    // View inquiry details
    @GetMapping("/inquiries/view/{id}")
    public String viewInquiryDetails(@PathVariable("id") Integer id, Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        try {
            Map<String, Object> inquiryDetails = eventService.getInquiryDetails(id);

            // Check if event already exists for this inquiry
            boolean eventExists = eventService.eventExistsForInquiry(id);

            model.addAttribute("inquiry", inquiryDetails);
            model.addAttribute("eventExists", eventExists);
            model.addAttribute("pageTitle", "Inquiry Details - ID: " + id);
            return "event-coordinator/inquiry-details";
        } catch (Exception e) {
            return "redirect:/event-coordinator/dashboard?error=inquiry_not_found";
        }
    }

    // Create event from inquiry
    @PostMapping("/events/create")
    public String createEventFromInquiry(
            @RequestParam Integer inquiryId,
            @RequestParam String eventType,
            @RequestParam String eventDate,
            @RequestParam Integer customerId,
            RedirectAttributes redirectAttributes) {

        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        try {
            // Check if event already exists for this inquiry
            if (eventService.eventExistsForInquiry(inquiryId)) {
                redirectAttributes.addFlashAttribute("error", "An event already exists for this inquiry!");
                return "redirect:/event-coordinator/inquiries/view/" + inquiryId;
            }

            // Create the event
            eventService.createEvent(eventType, eventDate, customerId, inquiryId);

            // Update inquiry status to "Accepted"
            eventService.updateInquiryStatus(inquiryId, "Accepted");

            redirectAttributes.addFlashAttribute("success", "Event created successfully and inquiry accepted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create event: " + e.getMessage());
        }

        return "redirect:/event-coordinator/dashboard";
    }

    // Reject inquiry
    @PostMapping("/inquiries/reject")
    public String rejectInquiry(
            @RequestParam Integer inquiryId,
            RedirectAttributes redirectAttributes) {

        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        try {
            eventService.updateInquiryStatus(inquiryId, "Rejected");
            redirectAttributes.addFlashAttribute("success", "Inquiry rejected successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to reject inquiry: " + e.getMessage());
        }

        return "redirect:/event-coordinator/dashboard";
    }
}
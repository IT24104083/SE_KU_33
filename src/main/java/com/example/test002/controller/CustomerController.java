package com.example.test002.controller;

import com.example.test002.model.Customer;
import com.example.test002.service.CustomerService;
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
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Customer Profile Page - Enhanced version
    @GetMapping("/profile")
    public String customerProfile(Model model) {
        Map<String, Object> customer = (Map<String, Object>) sessionManager.getAttribute("customer");
        if (customer == null) {
            return "redirect:/customer/login";
        }

        Integer customerId = (Integer) customer.get("CustomerID");

        // Get customer events for feedback
        List<Map<String, Object>> events = customerService.getCustomerEventsForFeedback(customerId);

        // Get customer's previous feedbacks and complaints
        List<Map<String, Object>> feedbacks = customerService.getCustomerFeedbacks(customerId);
        List<Map<String, Object>> complaints = customerService.getCustomerComplaints(customerId);

        model.addAttribute("customer", customer);
        model.addAttribute("events", events);
        model.addAttribute("feedbacks", feedbacks);
        model.addAttribute("complaints", complaints);
        model.addAttribute("pageTitle", "My Profile");

        return "customer-profile";
    }

    // Show feedback form for specific event
    @GetMapping("/feedback/{eventId}")
    public String showFeedbackForm(@PathVariable Integer eventId, Model model) {
        Map<String, Object> customer = (Map<String, Object>) sessionManager.getAttribute("customer");
        if (customer == null) {
            return "redirect:/customer/login";
        }

        Integer customerId = (Integer) customer.get("CustomerID");

        try {
            Map<String, Object> event = customerService.getCustomerEvent(customerId, eventId);
            model.addAttribute("event", event);
            model.addAttribute("customer", customer);
            model.addAttribute("pageTitle", "Submit Feedback");
        } catch (Exception e) {
            model.addAttribute("error", "Event not found or you don't have access to this event.");
        }

        return "customer/feedback-form";
    }

    // Show complaint form
    @GetMapping("/complaint")
    public String showComplaintForm(Model model) {
        Map<String, Object> customer = (Map<String, Object>) sessionManager.getAttribute("customer");
        if (customer == null) {
            return "redirect:/customer/login";
        }

        model.addAttribute("customer", customer);
        model.addAttribute("pageTitle", "Submit Complaint");
        return "customer/complaint-form";
    }

    // Submit complaint - This is unique to CustomerController
    @PostMapping("/complaint/submit")
    public String submitComplaint(@RequestParam String description,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        Map<String, Object> customer = (Map<String, Object>) sessionManager.getAttribute("customer");
        if (customer == null) {
            return "redirect:/customer/login";
        }

        Integer customerId = (Integer) customer.get("CustomerID");

        try {
            boolean success = customerService.submitComplaint(customerId, description, "General");

            if (success) {
                redirectAttributes.addFlashAttribute("message", "Your complaint has been submitted. We will get back to you soon.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to submit complaint. Please try again.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error submitting complaint: " + e.getMessage());
        }

        return "redirect:/customer/profile";
    }

    // View event details - Unique to CustomerController
    @GetMapping("/event-details/{eventId}")
    public String viewEventDetails(@PathVariable Integer eventId, Model model) {
        Map<String, Object> customer = (Map<String, Object>) sessionManager.getAttribute("customer");
        if (customer == null) {
            return "redirect:/customer/login";
        }

        Integer customerId = (Integer) customer.get("CustomerID");

        try {
            String sql = """
                SELECT e.EventID, e.EventType, e.EventDate, e.Status, e.VenueID,
                       v.Name as VenueName, v.Location, v.Capacity,
                       e.InquiryID, ei.GuestCount, ei.Budget
                FROM Event e
                LEFT JOIN Venue v ON e.VenueID = v.VenueID
                LEFT JOIN EventInquiry ei ON e.InquiryID = ei.InquiryID
                WHERE e.EventID = ? AND e.CustomerID = ?
                """;

            Map<String, Object> event = jdbcTemplate.queryForMap(sql, eventId, customerId);

            // Get vendors for this event
            String vendorSql = """
                SELECT ev.ServiceType, v.VendorName, v.ContactNo, v.Price
                FROM EventVendor ev
                JOIN Vendor v ON ev.VendorID = v.VendorID
                WHERE ev.EventID = ?
                """;

            List<Map<String, Object>> vendors = jdbcTemplate.queryForList(vendorSql, eventId);

            model.addAttribute("event", event);
            model.addAttribute("vendors", vendors);
            model.addAttribute("customer", customer);
            model.addAttribute("pageTitle", "Event Details");

        } catch (Exception e) {
            model.addAttribute("error", "Event not found or you don't have access to this event.");
        }

        return "customer/event-details";
    }
}
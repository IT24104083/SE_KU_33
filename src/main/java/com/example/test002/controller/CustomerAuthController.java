package com.example.test002.controller;

import com.example.test002.config.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/customer")
public class CustomerAuthController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SessionManager sessionManager;

    // Show login page
    @GetMapping("/login")
    public String showLoginForm() {
        return "customer-login";
    }

    // Handle login
    @PostMapping("/login")
    public String loginCustomer(@RequestParam String email,
                                @RequestParam String password,
                                Model model) {
        try {
            String sql = "SELECT * FROM Customer WHERE Email = ? AND Password = ?";
            Map<String, Object> customer = jdbcTemplate.queryForMap(sql, email, password);

            if (customer != null) {
                sessionManager.setAttribute("customer", customer);
                return "redirect:/customer/profile";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Invalid email or password!");
            return "customer-login";
        }
        model.addAttribute("error", "Invalid email or password!");
        return "customer-login";
    }

    // Show read-only profile and feedback form
    @GetMapping("/profile")
    public String viewProfile(Model model) {
        Map<String, Object> customer = (Map<String, Object>) sessionManager.getAttribute("customer");
        if (customer == null) {
            return "redirect:/customer/login";
        }

        // Fetch customer's latest event for feedback (optional)
        Integer customerId = (Integer) customer.get("CustomerID");
        String eventSql = "SELECT TOP 1 EventID, EventType, EventDate FROM Event WHERE CustomerID = ? ORDER BY EventDate DESC";
        Map<String, Object> event = null;
        try {
            event = jdbcTemplate.queryForMap(eventSql, customerId);
        } catch (Exception ignored) {}

        model.addAttribute("customer", customer);
        model.addAttribute("event", event);
        return "customer-profile";
    }

    // Handle feedback submission
    @PostMapping("/feedback/submit")
    public String submitFeedback(@RequestParam int eventId,
                                 @RequestParam String issueDescription,
                                 @RequestParam int rating,
                                 @RequestParam(required = false) String comments,
                                 Model model) {

        Map<String, Object> customer = (Map<String, Object>) sessionManager.getAttribute("customer");
        if (customer == null) {
            return "redirect:/customer/login";
        }

        Integer customerId = (Integer) customer.get("CustomerID");

        try {
            String sql = "INSERT INTO Feedback (CustomerID, EventID, IssueDescription, Rating, Comments) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, customerId, eventId, issueDescription, rating, comments);
            model.addAttribute("message", "Feedback submitted successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Error submitting feedback: " + e.getMessage());
        }

        // Reload profile after submission
        model.addAttribute("customer", customer);
        String eventSql = "SELECT TOP 1 EventID, EventType, EventDate FROM Event WHERE CustomerID = ? ORDER BY EventDate DESC";
        try {
            Map<String, Object> event = jdbcTemplate.queryForMap(eventSql, customerId);
            model.addAttribute("event", event);
        } catch (Exception ignored) {}
        return "customer-profile";
    }

    // Logout
    @GetMapping("/logout")
    public String logoutCustomer() {
        sessionManager.invalidate();
        return "redirect:/customer/login";
    }
}
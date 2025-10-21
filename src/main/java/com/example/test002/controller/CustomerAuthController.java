package com.example.test002.controller;

import com.example.test002.config.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    // REMOVED: Duplicate profile method - Now using CustomerController's enhanced profile

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
            String sql = "INSERT INTO Feedback (CustomerID, EventID, IssueDescription, Rating, Comments, Status, DateSubmitted) VALUES (?, ?, ?, ?, ?, 'New', GETDATE())";
            jdbcTemplate.update(sql, customerId, eventId, issueDescription, rating, comments);
            model.addAttribute("message", "Feedback submitted successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Error submitting feedback: " + e.getMessage());
        }

        // Redirect to profile page instead of reloading
        return "redirect:/customer/profile?success=feedback_submitted";
    }

    // View customer invoices
    @GetMapping("/invoices")
    public String viewCustomerInvoices(Model model) {
        Map<String, Object> customer = (Map<String, Object>) sessionManager.getAttribute("customer");
        if (customer == null) {
            return "redirect:/customer/login";
        }

        Integer customerId = (Integer) customer.get("CustomerID");

        try {
            String sql = """
            SELECT i.*, e.EventType, e.EventDate
            FROM Invoice i
            JOIN Event e ON i.EventID = e.EventID
            WHERE i.CustomerID = ?
            ORDER BY i.IssueDate DESC
            """;

            List<Map<String, Object>> invoices = jdbcTemplate.queryForList(sql, customerId);

            // Convert java.sql.Date to java.util.Date for Thymeleaf
            for (Map<String, Object> invoice : invoices) {
                if (invoice.get("EventDate") instanceof java.sql.Date) {
                    java.sql.Date sqlDate = (java.sql.Date) invoice.get("EventDate");
                    invoice.put("EventDate", new java.util.Date(sqlDate.getTime()));
                }
                if (invoice.get("IssueDate") instanceof java.sql.Date) {
                    java.sql.Date sqlDate = (java.sql.Date) invoice.get("IssueDate");
                    invoice.put("IssueDate", new java.util.Date(sqlDate.getTime()));
                }
                if (invoice.get("DueDate") instanceof java.sql.Date) {
                    java.sql.Date sqlDate = (java.sql.Date) invoice.get("DueDate");
                    invoice.put("DueDate", new java.util.Date(sqlDate.getTime()));
                }
            }

            model.addAttribute("invoices", invoices);
            model.addAttribute("customer", customer);

        } catch (Exception e) {
            model.addAttribute("error", "Error loading invoices: " + e.getMessage());
        }

        return "customer/invoices";
    }

    // Logout
    @GetMapping("/logout")
    public String logoutCustomer() {
        sessionManager.invalidate();
        return "redirect:/customer/login";
    }
}
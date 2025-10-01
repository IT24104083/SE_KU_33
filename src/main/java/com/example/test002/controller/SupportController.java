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
@RequestMapping("/support")
public class SupportController {

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Customer Support Officer Dashboard after login
    @GetMapping("/dashboard")
    public String supportDashboard(@RequestParam(value = "login", required = false) String login,
                                   Model model) {
        // Check if user is logged in
        Object userRole = sessionManager.getAttribute("userRole");
        Object userEmail = sessionManager.getAttribute("userEmail");

        if (userRole == null) {
            return "redirect:/login?error=session_expired";
        }

        if (!"CustomerSupportOfficer".equals(userRole.toString())) {
            return "redirect:/login?error=unauthorized";
        }

        if (login != null) {
            model.addAttribute("loginSuccess", true);
        }

        // Load dashboard statistics
        loadDashboardStats(model);

        model.addAttribute("pageTitle", "Customer Support Officer Dashboard");
        model.addAttribute("userEmail", userEmail);
        model.addAttribute("userRole", userRole);

        return "support/dashboard";
    }

    // Manage Complaints - Display all complaints from Complaint table
    @GetMapping("/complaints")
    public String manageComplaints(Model model) {
        // Check authorization
        Object userRole = sessionManager.getAttribute("userRole");
        if (userRole == null || !"CustomerSupportOfficer".equals(userRole.toString())) {
            return "redirect:/login?error=unauthorized";
        }

        // Load complaints data from Complaint table
        String sql = """
            SELECT c.ComplaintID, c.Description, c.DateSubmitted, c.Status, c.Response,
                   cust.CustomerID, cust.Customer_Name, cust.Email, cust.Phone
            FROM Complaint c
            INNER JOIN Customer cust ON c.CustomerID = cust.CustomerID
            ORDER BY c.DateSubmitted DESC
            """;

        List<Map<String, Object>> complaints = jdbcTemplate.queryForList(sql);
        model.addAttribute("complaints", complaints);
        model.addAttribute("pageTitle", "Manage Complaints");
        model.addAttribute("userEmail", sessionManager.getAttribute("userEmail"));
        model.addAttribute("userRole", userRole);

        return "support/complaints";
    }

    // Manage Feedbacks - Display all feedback from Feedback table
    @GetMapping("/feedbacks")
    public String manageFeedbacks(Model model) {
        // Check authorization
        Object userRole = sessionManager.getAttribute("userRole");
        if (userRole == null || !"CustomerSupportOfficer".equals(userRole.toString())) {
            return "redirect:/login?error=unauthorized";
        }

        // Load feedback data from Feedback table
        String sql = """
            SELECT f.FeedbackID, f.IssueDescription, f.Rating, f.Comments, f.DateSubmitted, 
                   f.Status, f.Response, f.EventID, f.CustomerID,
                   cust.Customer_Name, cust.Email, 
                   e.EventType, e.EventDate
            FROM Feedback f
            INNER JOIN Customer cust ON f.CustomerID = cust.CustomerID
            LEFT JOIN Event e ON f.EventID = e.EventID
            ORDER BY f.DateSubmitted DESC
            """;

        List<Map<String, Object>> feedbackList = jdbcTemplate.queryForList(sql);
        model.addAttribute("feedbackList", feedbackList);
        model.addAttribute("pageTitle", "Manage Feedbacks");
        model.addAttribute("userEmail", sessionManager.getAttribute("userEmail"));
        model.addAttribute("userRole", userRole);

        return "support/feedbacks";
    }

    // Show form to respond to a specific complaint
    @GetMapping("/complaints/respond")
    public String showComplaintResponseForm(@RequestParam Integer id, Model model) {
        // Check authorization
        Object userRole = sessionManager.getAttribute("userRole");
        if (userRole == null || !"CustomerSupportOfficer".equals(userRole.toString())) {
            return "redirect:/login?error=unauthorized";
        }

        // Load specific complaint details
        String sql = """
            SELECT c.ComplaintID, c.Description, c.DateSubmitted, c.Status, c.Response,
                   cust.Customer_Name, cust.Email, cust.Phone
            FROM Complaint c
            INNER JOIN Customer cust ON c.CustomerID = cust.CustomerID
            WHERE c.ComplaintID = ?
            """;

        Map<String, Object> complaint = jdbcTemplate.queryForMap(sql, id);
        model.addAttribute("complaint", complaint);
        model.addAttribute("pageTitle", "Respond to Complaint");
        model.addAttribute("userEmail", sessionManager.getAttribute("userEmail"));
        model.addAttribute("userRole", userRole);

        return "support/respond-complaint";
    }

    // Update complaint response
    @PostMapping("/complaints/respond")
    public String respondToComplaint(@RequestParam Integer complaintId,
                                     @RequestParam String response,
                                     Model model) {
        // Check authorization
        Object userRole = sessionManager.getAttribute("userRole");
        if (userRole == null || !"CustomerSupportOfficer".equals(userRole.toString())) {
            return "redirect:/login?error=unauthorized";
        }

        String sql = "UPDATE Complaint SET Response = ?, Status = 'Resolved' WHERE ComplaintID = ?";
        jdbcTemplate.update(sql, response, complaintId);

        return "redirect:/support/complaints?success=response_submitted";
    }

    // Show form to respond to a specific feedback
    @GetMapping("/feedbacks/respond")
    public String showFeedbackResponseForm(@RequestParam Integer id, Model model) {
        // Check authorization
        Object userRole = sessionManager.getAttribute("userRole");
        if (userRole == null || !"CustomerSupportOfficer".equals(userRole.toString())) {
            return "redirect:/login?error=unauthorized";
        }

        // Load specific feedback details
        String sql = """
            SELECT f.FeedbackID, f.IssueDescription, f.Rating, f.Comments, f.DateSubmitted, 
                   f.Status, f.Response, f.EventID,
                   cust.Customer_Name, cust.Email, e.EventType, e.EventDate
            FROM Feedback f
            INNER JOIN Customer cust ON f.CustomerID = cust.CustomerID
            LEFT JOIN Event e ON f.EventID = e.EventID
            WHERE f.FeedbackID = ?
            """;

        Map<String, Object> feedback = jdbcTemplate.queryForMap(sql, id);
        model.addAttribute("feedback", feedback);
        model.addAttribute("pageTitle", "Respond to Feedback");
        model.addAttribute("userEmail", sessionManager.getAttribute("userEmail"));
        model.addAttribute("userRole", userRole);

        return "support/respond-feedback";
    }

    // Update feedback response
    @PostMapping("/feedbacks/respond")
    public String respondToFeedback(@RequestParam Integer feedbackId,
                                    @RequestParam String response,
                                    Model model) {
        // Check authorization
        Object userRole = sessionManager.getAttribute("userRole");
        if (userRole == null || !"CustomerSupportOfficer".equals(userRole.toString())) {
            return "redirect:/login?error=unauthorized";
        }

        String sql = "UPDATE Feedback SET Response = ?, Status = 'Responded' WHERE FeedbackID = ?";
        jdbcTemplate.update(sql, response, feedbackId);

        return "redirect:/support/feedbacks?success=response_submitted";
    }

    // Update complaint status (without response)
    @PostMapping("/complaints/update-status")
    public String updateComplaintStatus(@RequestParam Integer complaintId,
                                        @RequestParam String status,
                                        Model model) {
        // Check authorization
        Object userRole = sessionManager.getAttribute("userRole");
        if (userRole == null || !"CustomerSupportOfficer".equals(userRole.toString())) {
            return "redirect:/login?error=unauthorized";
        }

        String sql = "UPDATE Complaint SET Status = ? WHERE ComplaintID = ?";
        jdbcTemplate.update(sql, status, complaintId);

        return "redirect:/support/complaints?success=status_updated";
    }

    // Update feedback status (without response)
    @PostMapping("/feedbacks/update-status")
    public String updateFeedbackStatus(@RequestParam Integer feedbackId,
                                       @RequestParam String status,
                                       Model model) {
        // Check authorization
        Object userRole = sessionManager.getAttribute("userRole");
        if (userRole == null || !"CustomerSupportOfficer".equals(userRole.toString())) {
            return "redirect:/login?error=unauthorized";
        }

        String sql = "UPDATE Feedback SET Status = ? WHERE FeedbackID = ?";
        jdbcTemplate.update(sql, status, feedbackId);

        return "redirect:/support/feedbacks?success=status_updated";
    }

    // Filter complaints by status
    @GetMapping("/complaints/filter")
    public String filterComplaints(@RequestParam(required = false) String status,
                                   Model model) {
        // Check authorization
        Object userRole = sessionManager.getAttribute("userRole");
        if (userRole == null || !"CustomerSupportOfficer".equals(userRole.toString())) {
            return "redirect:/login?error=unauthorized";
        }

        String sql;
        List<Map<String, Object>> complaints;

        if (status != null && !status.isEmpty() && !"all".equals(status)) {
            sql = """
                SELECT c.ComplaintID, c.Description, c.DateSubmitted, c.Status, c.Response,
                       cust.Customer_Name, cust.Email, cust.Phone
                FROM Complaint c
                INNER JOIN Customer cust ON c.CustomerID = cust.CustomerID
                WHERE c.Status = ?
                ORDER BY c.DateSubmitted DESC
                """;
            complaints = jdbcTemplate.queryForList(sql, status);
        } else {
            sql = """
                SELECT c.ComplaintID, c.Description, c.DateSubmitted, c.Status, c.Response,
                       cust.Customer_Name, cust.Email, cust.Phone
                FROM Complaint c
                INNER JOIN Customer cust ON c.CustomerID = cust.CustomerID
                ORDER BY c.DateSubmitted DESC
                """;
            complaints = jdbcTemplate.queryForList(sql);
        }

        model.addAttribute("complaints", complaints);
        model.addAttribute("pageTitle", "Manage Complaints");
        model.addAttribute("selectedStatus", status);
        model.addAttribute("userEmail", sessionManager.getAttribute("userEmail"));
        model.addAttribute("userRole", userRole);

        return "support/complaints";
    }

    // Helper method to load dashboard statistics
    private void loadDashboardStats(Model model) {
        try {
            // Count new complaints
            String newComplaintsSql = "SELECT COUNT(*) FROM Complaint WHERE Status = 'New'";
            Integer newComplaints = jdbcTemplate.queryForObject(newComplaintsSql, Integer.class);
            model.addAttribute("newComplaints", newComplaints != null ? newComplaints : 0);

            // Count new feedback
            String newFeedbackSql = "SELECT COUNT(*) FROM Feedback WHERE Status = 'New'";
            Integer newFeedback = jdbcTemplate.queryForObject(newFeedbackSql, Integer.class);
            model.addAttribute("newFeedback", newFeedback != null ? newFeedback : 0);

            // Count total customers
            String totalCustomersSql = "SELECT COUNT(*) FROM Customer";
            Integer totalCustomers = jdbcTemplate.queryForObject(totalCustomersSql, Integer.class);
            model.addAttribute("totalCustomers", totalCustomers != null ? totalCustomers : 0);

            // Count resolved issues this month
            String resolvedThisMonthSql = """
                SELECT COUNT(*) FROM (
                    SELECT ComplaintID FROM Complaint 
                    WHERE Status = 'Resolved' AND MONTH(DateSubmitted) = MONTH(GETDATE()) AND YEAR(DateSubmitted) = YEAR(GETDATE())
                    UNION ALL
                    SELECT FeedbackID FROM Feedback 
                    WHERE Status = 'Responded' AND MONTH(DateSubmitted) = MONTH(GETDATE()) AND YEAR(DateSubmitted) = YEAR(GETDATE())
                ) AS resolved_issues
                """;
            Integer resolvedThisMonth = jdbcTemplate.queryForObject(resolvedThisMonthSql, Integer.class);
            model.addAttribute("resolvedThisMonth", resolvedThisMonth != null ? resolvedThisMonth : 0);
        } catch (Exception e) {
            // Handle any database errors gracefully
            model.addAttribute("newComplaints", 0);
            model.addAttribute("newFeedback", 0);
            model.addAttribute("totalCustomers", 0);
            model.addAttribute("resolvedThisMonth", 0);
        }
    }
}
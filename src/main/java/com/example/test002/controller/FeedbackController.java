package com.example.test002.controller;

import com.example.test002.config.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SessionManager sessionManager;

    // Show feedback form
    @GetMapping("/new")
    public String showFeedbackForm(Model model) {
        Map<String, Object> customer = (Map<String, Object>) sessionManager.getAttribute("customer");
        if (customer == null) {
            return "redirect:/customer/login";
        }
        return "customer/feedback-form";
    }

    // Submit feedback
    @PostMapping("/submit")
    public String submitFeedback(@RequestParam String feedbackText,
                                 @RequestParam int eventId,
                                 Model model) {
        Map<String, Object> customer = (Map<String, Object>) sessionManager.getAttribute("customer");
        if (customer == null) {
            return "redirect:/customer/login";
        }

        int customerId = (int) customer.get("id");

        String sql = "INSERT INTO Feedback (customerId, eventId, feedbackText) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, customerId, eventId, feedbackText);

        model.addAttribute("message", "Feedback submitted successfully!");
        return "redirect:/customer/profile";
    }
}


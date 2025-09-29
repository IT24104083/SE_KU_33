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
        // looks for customer-login.html under /templates
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

    // Show profile page
    @GetMapping("/profile")
    public String viewProfile(Model model) {
        Map<String, Object> customer = (Map<String, Object>) sessionManager.getAttribute("customer");
        if (customer == null) {
            return "redirect:/customer/login";
        }
        model.addAttribute("customer", customer);
        return "customer-profile"; // looks for customer-profile.html
    }

    // Logout
    @GetMapping("/logout")
    public String logoutCustomer() {
        sessionManager.invalidate();
        return "redirect:/customer/login";
    }
}




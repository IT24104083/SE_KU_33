package com.example.test002.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import com.example.test002.config.SessionManager;
import com.example.test002.strategy.LoginStrategy;
import com.example.test002.strategy.LoginStrategyFactory;

import java.util.Map;

@Controller
public class AuthController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private LoginStrategyFactory loginStrategyFactory; // NEW: Strategy Factory

    // Login page
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "email", required = false) String email,
                            Model model) {

        if (error != null) {
            model.addAttribute("error", "Invalid email or password!");
        }
        if (logout != null) {
            model.addAttribute("logout", "You have been logged out successfully.");
        }
        if (email != null) {
            model.addAttribute("email", email);
        }

        return "login";
    }

    // Login processing - MODIFIED: Using Strategy Pattern
    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        Model model) {

        System.out.println("Login attempt: " + email);

        try {
            String sql = "SELECT UserID, Email, UserRole FROM SystemUsers WHERE Email = ? AND Password = ? AND IsActive = 1";

            Map<String, Object> user = jdbcTemplate.queryForMap(sql, email, password);

            if (user != null) {
                // Store user in session
                sessionManager.setAttribute("userId", user.get("UserID"));
                sessionManager.setAttribute("userEmail", user.get("Email"));
                sessionManager.setAttribute("userRole", user.get("UserRole"));

                String userRole = (String) user.get("UserRole");
                System.out.println("Login successful! Role: " + userRole);

                // NEW: Use Strategy Pattern instead of switch statement
                try {
                    LoginStrategy strategy = loginStrategyFactory.getStrategy(userRole);
                    return strategy.getRedirectUrl();
                } catch (IllegalArgumentException e) {
                    model.addAttribute("error", "Invalid user role: " + userRole);
                    return "login";
                }
            }
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
            model.addAttribute("error", "Invalid email or password");
            model.addAttribute("email", email);
            return "login";
        }

        model.addAttribute("error", "Invalid email or password");
        return "login";
    }

    // Customer Consultant Dashboard
    @GetMapping("/consultant/dashboard")
    public String consultantDashboard(@RequestParam(value = "login", required = false) String login,
                                      Model model) {

        // Check if user is logged in and has the right role
        Object userRole = sessionManager.getAttribute("userRole");
        Object userEmail = sessionManager.getAttribute("userEmail");

        if (userRole == null) {
            return "redirect:/login?error=session_expired";
        }

        if (!"CustomerConsultant".equals(userRole.toString())) {
            return "redirect:/login?error=unauthorized";
        }

        if (login != null) {
            model.addAttribute("loginSuccess", true);
        }

        model.addAttribute("pageTitle", "Customer Consultant Dashboard");
        model.addAttribute("userEmail", userEmail);
        model.addAttribute("userRole", userRole);
        return "consultant/dashboard";
    }

    // Logout
    @GetMapping("/logout")
    public String logout() {
        sessionManager.invalidate();
        return "redirect:/login?logout=true";
    }
}
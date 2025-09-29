package com.example.test002.controller;

import com.example.test002.model.Vendor;
import com.example.test002.service.VendorService;
import com.example.test002.config.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/vendors")
public class VendorController {

    @Autowired
    private VendorService vendorService;

    @Autowired
    private SessionManager sessionManager;

    // Vendor dashboard after login
    @GetMapping("/dashboard")
    public String vendorDashboard(@RequestParam(value = "login", required = false) String login,
                                  Model model) {
        // Check if user is logged in
        Object userRole = sessionManager.getAttribute("userRole");
        Object userEmail = sessionManager.getAttribute("userEmail");

        if (userRole == null) {
            return "redirect:/login?error=session_expired";
        }

        if (!"VendorCoordinator".equals(userRole.toString())) {
            return "redirect:/login?error=unauthorized";
        }

        if (login != null) {
            model.addAttribute("loginSuccess", true);
        }

        model.addAttribute("pageTitle", "Vendor Dashboard");
        model.addAttribute("userEmail", userEmail);
        model.addAttribute("userRole", userRole);

        // Load vendors into dashboard
        List<Vendor> vendors = vendorService.getAllVendors();
        model.addAttribute("vendors", vendors);

        return "vendor/vendor-list"; // Use your vendor-list.html as dashboard
    }

    // List all vendors
    @GetMapping
    public String listVendors(Model model) {
        List<Vendor> vendors = vendorService.getAllVendors();
        model.addAttribute("vendors", vendors);
        return "vendor/vendor-list";
    }

    // Show add vendor form
    @GetMapping("/new")
    public String addVendorForm(Model model) {
        model.addAttribute("vendor", new Vendor());
        return "vendor/add-vendor";
    }

    // Save new vendor
    @PostMapping("/add")
    public String saveVendor(@ModelAttribute Vendor vendor) {
        vendorService.addVendor(vendor);
        return "redirect:/vendors";
    }

    // Show edit vendor form
    @GetMapping("/edit/{id}")
    public String editVendorForm(@PathVariable("id") Integer id, Model model) {
        Vendor vendor = vendorService.getVendorById(id);
        model.addAttribute("vendor", vendor);
        return "vendor/edit-vendor";
    }

    // Update vendor
    @PostMapping("/edit")
    public String updateVendor(@ModelAttribute Vendor vendor) {
        vendorService.updateVendor(vendor);
        return "redirect:/vendors";
    }

    // Delete vendor
    @GetMapping("/delete/{id}")
    public String deleteVendor(@PathVariable("id") Integer id) {
        vendorService.deleteVendor(id);
        return "redirect:/vendors";
    }

    // Filter vendors by type and/or price range
    @GetMapping("/filter")
    public String filterVendors(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Model model
    ) {
        List<Vendor> vendors = vendorService.getAllVendors();

        if (type != null && !type.isEmpty()) {
            vendors.removeIf(v -> !v.getVendorType().equalsIgnoreCase(type));
        }

        if (minPrice != null) {
            vendors.removeIf(v -> v.getPrice().compareTo(minPrice) < 0);
        }

        if (maxPrice != null) {
            vendors.removeIf(v -> v.getPrice().compareTo(maxPrice) > 0);
        }

        model.addAttribute("vendors", vendors);
        model.addAttribute("selectedType", type);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        return "vendor/vendor-list";
    }
}


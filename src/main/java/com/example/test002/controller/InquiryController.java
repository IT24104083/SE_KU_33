package com.example.test002.controller;

import com.example.test002.model.Inquiry;
import com.example.test002.model.Customer;
import com.example.test002.service.InquiryService;
import com.example.test002.service.CustomerService;
import com.example.test002.config.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/consultant/inquiries")
public class InquiryController {

    @Autowired
    private InquiryService inquiryService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private SessionManager sessionManager;

    // Check if user is authorized
    private boolean isAuthorized() {
        Object userRole = sessionManager.getAttribute("userRole");
        return userRole != null && "CustomerConsultant".equals(userRole.toString());
    }

    // List all inquiries
    @GetMapping
    public String listInquiries(Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        // Debug database status
        inquiryService.debugDatabaseStatus();

        List<Inquiry> inquiries = inquiryService.getAllInquiries();

        System.out.println("DEBUG: Controller - Received " + inquiries.size() + " inquiries from service");
        for (Inquiry inquiry : inquiries) {
            System.out.println("DEBUG: Controller - Inquiry ID: " + inquiry.getInquiryID() +
                    ", CustomerID: " + inquiry.getCustomerID() +
                    ", Status: " + inquiry.getStatus());
        }

        model.addAttribute("inquiries", inquiries);
        model.addAttribute("pageTitle", "Manage Inquiries");
        model.addAttribute("totalInquiries", inquiryService.getInquiriesCount());
        model.addAttribute("pendingInquiries", inquiryService.getPendingInquiriesCount());

        return "consultant/inquiries-list";
    }

    // Show create inquiry form
    @GetMapping("/new")
    public String showCreateInquiryForm(Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        // Get all customers for dropdown
        List<Customer> customers = customerService.getAllCustomers();

        model.addAttribute("inquiry", new Inquiry());
        model.addAttribute("customers", customers);
        model.addAttribute("pageTitle", "Create New Inquiry");
        model.addAttribute("today", java.time.LocalDate.now());

        return "consultant/inquiry-form";
    }

    // Handle form submission
    @PostMapping("/save")
    public String saveInquiry(@ModelAttribute Inquiry inquiry,
                              @RequestParam(required = false) Boolean photographer,
                              @RequestParam(required = false) Boolean catering,
                              @RequestParam(required = false) Boolean dj,
                              @RequestParam(required = false) Boolean decorations,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        try {
            // Set boolean fields (checkboxes)
            inquiry.setPhotographer(photographer != null && photographer);
            inquiry.setCatering(catering != null && catering);
            inquiry.setDj(dj != null && dj);
            inquiry.setDecorations(decorations != null && decorations);

            // Set default status
            inquiry.setStatus("Pending");

            Inquiry savedInquiry = inquiryService.createInquiry(inquiry);
            redirectAttributes.addFlashAttribute("success",
                    "Inquiry #" + savedInquiry.getInquiryID() + " created successfully!");

            return "redirect:/consultant/inquiries";

        } catch (Exception e) {
            // Get customers again for dropdown
            List<Customer> customers = customerService.getAllCustomers();
            model.addAttribute("customers", customers);
            model.addAttribute("today", java.time.LocalDate.now());

            model.addAttribute("error", "Error creating inquiry: " + e.getMessage());
            model.addAttribute("inquiry", inquiry);
            return "consultant/inquiry-form";
        }
    }

    // View inquiry details
    @GetMapping("/view/{id}")
    public String viewInquiry(@PathVariable("id") Integer id, Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        try {
            Inquiry inquiry = inquiryService.getInquiryById(id);
            Customer customer = customerService.getCustomerById(inquiry.getCustomerID());

            model.addAttribute("inquiry", inquiry);
            model.addAttribute("customer", customer);
            model.addAttribute("pageTitle", "Inquiry Details - #" + id);

        } catch (Exception e) {
            model.addAttribute("error", "Inquiry not found with ID: " + id);
        }

        return "consultant/inquiry-view";
    }

    // Get inquiries by customer (AJAX endpoint)
    @GetMapping("/customer/{customerId}")
    @ResponseBody
    public List<Inquiry> getInquiriesByCustomer(@PathVariable("customerId") Integer customerId) {
        return inquiryService.getInquiriesByCustomerId(customerId);
    }
}
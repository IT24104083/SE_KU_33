package com.example.test002.controller;

import com.example.test002.model.Customer;
import com.example.test002.model.Inquiry;
import com.example.test002.service.CustomerService;
import com.example.test002.service.InquiryService;
import com.example.test002.config.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/consultant")
public class CustomerConsultantController {

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

    // Dashboard - CHANGED MAPPING to avoid conflict
    @GetMapping("/home")
    public String dashboard(Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        // Add statistics for dashboard
        int totalInquiries = inquiryService.getInquiriesCount();
        int pendingInquiries = inquiryService.getPendingInquiriesCount();
        int totalCustomers = customerService.getCustomersCount();

        model.addAttribute("totalInquiries", totalInquiries);
        model.addAttribute("pendingInquiries", pendingInquiries);
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("userEmail", sessionManager.getAttribute("userEmail"));
        model.addAttribute("userRole", sessionManager.getAttribute("userRole"));
        model.addAttribute("pageTitle", "Customer Consultant Dashboard");
        model.addAttribute("loginSuccess", true);

        return "consultant/dashboard";
    }

    // Customer Management
    @GetMapping("/customers")
    public String listCustomers(Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        List<Customer> customers = customerService.getAllCustomers();
        model.addAttribute("customers", customers);
        model.addAttribute("pageTitle", "Manage Customers");
        return "consultant/customers-list";
    }

    @GetMapping("/customers/new")
    public String showAddCustomerForm(Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        model.addAttribute("customer", new Customer());
        model.addAttribute("pageTitle", "Add New Customer");
        return "consultant/customer-form";
    }

    @PostMapping("/customers/save")
    public String saveCustomer(@ModelAttribute Customer customer,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        try {
            // Check if email already exists
            if (customerService.emailExists(customer.getEmail())) {
                model.addAttribute("error", "Email already exists!");
                model.addAttribute("customer", customer);
                model.addAttribute("pageTitle", "Add New Customer");
                return "consultant/customer-form";
            }

            Customer savedCustomer = customerService.createCustomer(customer);
            redirectAttributes.addFlashAttribute("success",
                    "Customer '" + savedCustomer.getCustomerName() + "' created successfully!");

            return "redirect:/consultant/customers";

        } catch (Exception e) {
            model.addAttribute("error", "Error creating customer: " + e.getMessage());
            model.addAttribute("customer", customer);
            model.addAttribute("pageTitle", "Add New Customer");
            return "consultant/customer-form";
        }
    }

    @GetMapping("/customers/view/{id}")
    public String viewCustomer(@PathVariable("id") Integer id, Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        try {
            Customer customer = customerService.getCustomerById(id);
            // Get customer's inquiries
            List<Inquiry> customerInquiries = inquiryService.getInquiriesByCustomerId(id);

            model.addAttribute("customer", customer);
            model.addAttribute("inquiries", customerInquiries);
            model.addAttribute("pageTitle", "Customer Details - " + customer.getCustomerName());
        } catch (Exception e) {
            model.addAttribute("error", "Customer not found with ID: " + id);
        }

        return "consultant/customer-view";
    }

    @GetMapping("/customers/edit/{id}")
    public String editCustomer(@PathVariable("id") Integer id, Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        try {
            Customer customer = customerService.getCustomerById(id);
            model.addAttribute("customer", customer);
            model.addAttribute("pageTitle", "Edit Customer - " + customer.getCustomerName());
        } catch (Exception e) {
            model.addAttribute("error", "Customer not found with ID: " + id);
            return "redirect:/consultant/customers";
        }

        return "consultant/customer-edit";
    }

    @PostMapping("/customers/update")
    public String updateCustomer(@ModelAttribute Customer customer,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        try {
            customerService.updateCustomer(customer);
            redirectAttributes.addFlashAttribute("success",
                    "Customer '" + customer.getCustomerName() + "' updated successfully!");
            return "redirect:/consultant/customers";
        } catch (Exception e) {
            model.addAttribute("error", "Error updating customer: " + e.getMessage());
            model.addAttribute("customer", customer);
            model.addAttribute("pageTitle", "Edit Customer - " + customer.getCustomerName());
            return "consultant/customer-edit";
        }
    }

    @GetMapping("/customers/delete/{id}")
    public String deleteCustomer(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        try {
            customerService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("success", "Customer deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting customer: " + e.getMessage());
        }

        return "redirect:/consultant/customers";
    }

    // Inquiry Management - Using your existing InquiryService
    @GetMapping("/inquiries")
    public String listInquiries(Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        List<Inquiry> inquiries = inquiryService.getAllInquiries();
        int totalInquiries = inquiryService.getInquiriesCount();
        int pendingInquiries = inquiryService.getPendingInquiriesCount();

        model.addAttribute("inquiries", inquiries);
        model.addAttribute("totalInquiries", totalInquiries);
        model.addAttribute("pendingInquiries", pendingInquiries);
        model.addAttribute("pageTitle", "Manage Event Inquiries");

        return "consultant/inquiries-list";
    }

    @GetMapping("/inquiries/new")
    public String showCreateInquiryForm(Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        List<Customer> customers = customerService.getAllCustomers();
        model.addAttribute("inquiry", new Inquiry());
        model.addAttribute("customers", customers);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("pageTitle", "Create New Inquiry");

        return "consultant/inquiry-form";
    }

    @PostMapping("/inquiries/save")
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
            List<Customer> customers = customerService.getAllCustomers();
            model.addAttribute("error", "Error creating inquiry: " + e.getMessage());
            model.addAttribute("inquiry", inquiry);
            model.addAttribute("customers", customers);
            model.addAttribute("today", LocalDate.now());
            model.addAttribute("pageTitle", "Create New Inquiry");
            return "consultant/inquiry-form";
        }
    }

    @GetMapping("/inquiries/view/{id}")
    public String viewInquiry(@PathVariable("id") Integer id, Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        try {
            Inquiry inquiry = inquiryService.getInquiryById(id);
            Customer customer = customerService.getCustomerById(inquiry.getCustomerID());

            model.addAttribute("inquiry", inquiry);
            model.addAttribute("customer", customer);
            model.addAttribute("pageTitle", "Inquiry Details - #" + inquiry.getInquiryID());
        } catch (Exception e) {
            model.addAttribute("error", "Inquiry not found with ID: " + id);
            return "redirect:/consultant/inquiries";
        }

        return "consultant/inquiry-view";
    }

    @GetMapping("/inquiries/edit/{id}")
    public String editInquiry(@PathVariable("id") Integer id, Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        try {
            Inquiry inquiry = inquiryService.getInquiryById(id);
            List<Customer> customers = customerService.getAllCustomers();

            model.addAttribute("inquiry", inquiry);
            model.addAttribute("customers", customers);
            model.addAttribute("today", LocalDate.now());
            model.addAttribute("pageTitle", "Edit Inquiry - #" + inquiry.getInquiryID());
        } catch (Exception e) {
            model.addAttribute("error", "Inquiry not found with ID: " + id);
            return "redirect:/consultant/inquiries";
        }

        return "consultant/inquiry-edit";
    }

    @PostMapping("/inquiries/update")
    public String updateInquiry(@ModelAttribute Inquiry inquiry,
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
            // Set boolean fields
            inquiry.setPhotographer(photographer != null && photographer);
            inquiry.setCatering(catering != null && catering);
            inquiry.setDj(dj != null && dj);
            inquiry.setDecorations(decorations != null && decorations);

            // Update the inquiry - you'll need to add update method to InquiryService
            inquiryService.updateInquiryStatus(inquiry.getInquiryID(), inquiry.getStatus());
            redirectAttributes.addFlashAttribute("success",
                    "Inquiry #" + inquiry.getInquiryID() + " updated successfully!");
            return "redirect:/consultant/inquiries";
        } catch (Exception e) {
            List<Customer> customers = customerService.getAllCustomers();
            model.addAttribute("error", "Error updating inquiry: " + e.getMessage());
            model.addAttribute("inquiry", inquiry);
            model.addAttribute("customers", customers);
            model.addAttribute("today", LocalDate.now());
            model.addAttribute("pageTitle", "Edit Inquiry - #" + inquiry.getInquiryID());
            return "consultant/inquiry-edit";
        }
    }

    @GetMapping("/inquiries/delete/{id}")
    public String deleteInquiry(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        try {
            inquiryService.deleteInquiry(id);
            redirectAttributes.addFlashAttribute("success", "Inquiry deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting inquiry: " + e.getMessage());
        }

        return "redirect:/consultant/inquiries";
    }

    // Update inquiry status
    @PostMapping("/inquiries/update-status")
    public String updateInquiryStatus(@RequestParam Integer inquiryId,
                                      @RequestParam String status,
                                      RedirectAttributes redirectAttributes) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        try {
            inquiryService.updateInquiryStatus(inquiryId, status);
            redirectAttributes.addFlashAttribute("success",
                    "Inquiry status updated to: " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error updating inquiry status: " + e.getMessage());
        }

        return "redirect:/consultant/inquiries";
    }

    // Simple dashboard for testing - CHANGED MAPPING
    @GetMapping("/simple-dashboard")
    public String simpleDashboard(Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        model.addAttribute("pageTitle", "Simple Dashboard");
        return "consultant/simple-dashboard";
    }
}
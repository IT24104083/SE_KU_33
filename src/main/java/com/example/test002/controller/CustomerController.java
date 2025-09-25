package com.example.test002.controller;

import com.example.test002.model.Customer;
import com.example.test002.service.CustomerService;
import com.example.test002.config.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/consultant/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private SessionManager sessionManager;

    // Check if user is authorized
    private boolean isAuthorized() {
        Object userRole = sessionManager.getAttribute("userRole");
        return userRole != null && "CustomerConsultant".equals(userRole.toString());
    }

    // List all customers
    @GetMapping("")
    public String listCustomers(Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        List<Customer> customers = customerService.getAllCustomers();
        model.addAttribute("customers", customers);
        model.addAttribute("pageTitle", "Manage Customers");
        return "consultant/customers-list";
    }

    // Show add customer form
    @GetMapping("/new")
    public String showAddCustomerForm(Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        model.addAttribute("customer", new Customer());
        model.addAttribute("pageTitle", "Add New Customer");
        return "consultant/customer-form";
    }

    // Handle form submission
    @PostMapping("/save")
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
                return "consultant/customer-form";
            }

            Customer savedCustomer = customerService.createCustomer(customer);
            redirectAttributes.addFlashAttribute("success",
                    "Customer '" + savedCustomer.getCustomerName() + "' created successfully!");

            return "redirect:/consultant/customers";

        } catch (Exception e) {
            model.addAttribute("error", "Error creating customer: " + e.getMessage());
            model.addAttribute("customer", customer);
            return "consultant/customer-form";
        }
    }

    // View customer details - FIXED URL PATTERN
    @GetMapping("/view/{id}")
    public String viewCustomer(@PathVariable("id") Integer id, Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        try {
            Customer customer = customerService.getCustomerById(id);
            model.addAttribute("customer", customer);
            model.addAttribute("pageTitle", "Customer Details - " + customer.getCustomerName());
        } catch (Exception e) {
            model.addAttribute("error", "Customer not found with ID: " + id);
        }

        return "consultant/customer-view";
    }

    // Add this method for testing
    @GetMapping("/test")
    public String testPage(Model model) {
        model.addAttribute("message", "Test page is working!");
        return "consultant/test-page";
    }
}
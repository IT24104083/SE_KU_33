package com.example.test002.controller;

import com.example.test002.config.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/finance")
public class FinanceOfficerController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SessionManager sessionManager;

    private boolean isAuthorized() {
        Object userRole = sessionManager.getAttribute("userRole");
        return userRole != null && "FinanceOfficer".equals(userRole.toString());
    }

    // Finance Officer Dashboard
    @GetMapping("/dashboard")
    public String financeDashboard(Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        // Load dashboard statistics
        loadFinanceStats(model);

        model.addAttribute("pageTitle", "Finance Officer Dashboard");
        model.addAttribute("userEmail", sessionManager.getAttribute("userEmail"));
        model.addAttribute("userRole", sessionManager.getAttribute("userRole"));

        return "finance/dashboard";
    }

    // View confirmed events ready for billing
    @GetMapping("/confirmed-events")
    public String viewConfirmedEvents(Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        String sql = """
            SELECT e.EventID, e.EventType, e.EventDate, c.Customer_Name, c.Email, c.Phone,
                   v.Name as VenueName, v.VenueCost,
                   (SELECT SUM(vend.Price) FROM EventVendor ev 
                    JOIN Vendor vend ON ev.VendorID = vend.VendorID 
                    WHERE ev.EventID = e.EventID) as TotalVendorCost,
                   (SELECT COUNT(*) FROM Invoice WHERE EventID = e.EventID) as InvoiceExists
            FROM Event e
            JOIN Customer c ON e.CustomerID = c.CustomerID
            LEFT JOIN Venue v ON e.VenueID = v.VenueID
            WHERE e.Status = 'Confirmed'
            ORDER BY e.EventDate
            """;

        List<Map<String, Object>> confirmedEvents = jdbcTemplate.queryForList(sql);

        // NO DATE CONVERSION - use raw dates
        model.addAttribute("confirmedEvents", confirmedEvents);
        model.addAttribute("pageTitle", "Confirmed Events for Billing");

        return "finance/confirmed-events";
    }

    // Create invoice form for a specific event
    @GetMapping("/create-invoice/{eventId}")
    public String showCreateInvoiceForm(@PathVariable("eventId") Integer eventId, Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        try {
            // Get event details with costs
            String eventSql = """
                SELECT e.EventID, e.EventType, e.EventDate, c.CustomerID, c.Customer_Name, c.Email,
                       v.VenueID, v.Name as VenueName, v.VenueCost,
                       (SELECT SUM(vend.Price) FROM EventVendor ev 
                        JOIN Vendor vend ON ev.VendorID = vend.VendorID 
                        WHERE ev.EventID = e.EventID) as TotalVendorCost
                FROM Event e
                JOIN Customer c ON e.CustomerID = c.CustomerID
                LEFT JOIN Venue v ON e.VenueID = v.VenueID
                WHERE e.EventID = ?
                """;

            Map<String, Object> eventDetails = jdbcTemplate.queryForMap(eventSql, eventId);

            // NO DATE CONVERSION - use raw dates

            // Get assigned vendors details
            String vendorsSql = """
                SELECT ev.ServiceType, v.VendorName, v.Price
                FROM EventVendor ev
                JOIN Vendor v ON ev.VendorID = v.VendorID
                WHERE ev.EventID = ?
                ORDER BY ev.ServiceType
                """;

            List<Map<String, Object>> assignedVendors = jdbcTemplate.queryForList(vendorsSql, eventId);

            model.addAttribute("event", eventDetails);
            model.addAttribute("assignedVendors", assignedVendors);
            model.addAttribute("today", LocalDate.now());
            model.addAttribute("dueDate", LocalDate.now().plusDays(15)); // 15 days due date
            model.addAttribute("pageTitle", "Create Invoice - Event #" + eventId);

            return "finance/create-invoice";

        } catch (Exception e) {
            return "redirect:/finance/confirmed-events?error=event_not_found";
        }
    }

    // Generate invoice
    @PostMapping("/generate-invoice")
    public String generateInvoice(
            @RequestParam Integer eventId,
            @RequestParam Integer customerId,
            @RequestParam BigDecimal venueCost,
            @RequestParam BigDecimal vendorCost,
            @RequestParam BigDecimal additionalCharges,
            @RequestParam BigDecimal taxAmount,
            @RequestParam BigDecimal serviceCharges,
            @RequestParam BigDecimal otherCharges,
            @RequestParam String additionalComments,
            @RequestParam String dueDate,
            RedirectAttributes redirectAttributes) {

        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        try {
            // Calculate total amount
            BigDecimal totalPay = venueCost.add(vendorCost)
                    .add(additionalCharges)
                    .add(taxAmount)
                    .add(serviceCharges)
                    .add(otherCharges);

            // Check if invoice already exists for this event
            String checkSql = "SELECT COUNT(*) FROM Invoice WHERE EventID = ?";
            Integer invoiceCount = jdbcTemplate.queryForObject(checkSql, Integer.class, eventId);

            if (invoiceCount != null && invoiceCount > 0) {
                redirectAttributes.addFlashAttribute("error", "Invoice already exists for this event!");
                return "redirect:/finance/confirmed-events";
            }

            // Create invoice
            String sql = """
                INSERT INTO Invoice (EventID, CustomerID, IssueDate, DueDate, VenueCost, 
                VendorCost, AdditionalCharges, TaxAmount, ServiceCharges, OtherCharges, 
                Total_Pay, AdditionalComments, VerificationStatus, PaymentStatus)
                VALUES (?, ?, GETDATE(), ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Verified', 'Pending')
                """;

            jdbcTemplate.update(sql, eventId, customerId, dueDate, venueCost, vendorCost,
                    additionalCharges, taxAmount, serviceCharges, otherCharges,
                    totalPay, additionalComments);

            redirectAttributes.addFlashAttribute("success",
                    "Invoice created successfully! Total Amount: $" + totalPay);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Failed to create invoice: " + e.getMessage());
        }

        return "redirect:/finance/confirmed-events";
    }

    // View all invoices
    @GetMapping("/invoices")
    public String viewInvoices(Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        String sql = """
            SELECT i.*, e.EventType, e.EventDate, c.Customer_Name, c.Email,
                   (i.VenueCost + i.VendorCost + i.AdditionalCharges + 
                    i.TaxAmount + i.ServiceCharges + i.OtherCharges) as CalculatedTotal
            FROM Invoice i
            JOIN Event e ON i.EventID = e.EventID
            JOIN Customer c ON i.CustomerID = c.CustomerID
            ORDER BY i.IssueDate DESC
            """;

        List<Map<String, Object>> invoices = jdbcTemplate.queryForList(sql);

        // NO DATE CONVERSION - use raw dates
        model.addAttribute("invoices", invoices);
        model.addAttribute("pageTitle", "All Invoices");

        return "finance/invoices";
    }

    // Update payment status
    @PostMapping("/update-payment-status")
    public String updatePaymentStatus(
            @RequestParam Integer invoiceId,
            @RequestParam String paymentStatus,
            RedirectAttributes redirectAttributes) {

        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        try {
            String sql = "UPDATE Invoice SET PaymentStatus = ? WHERE InvoiceID = ?";
            jdbcTemplate.update(sql, paymentStatus, invoiceId);

            redirectAttributes.addFlashAttribute("success",
                    "Payment status updated to: " + paymentStatus);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Failed to update payment status: " + e.getMessage());
        }

        return "redirect:/finance/invoices";
    }

    // View invoice details
    @GetMapping("/invoice-details/{invoiceId}")
    public String viewInvoiceDetails(@PathVariable("invoiceId") Integer invoiceId, Model model) {
        if (!isAuthorized()) {
            return "redirect:/login?error=unauthorized";
        }

        try {
            String sql = """
                SELECT i.*, e.EventType, e.EventDate, c.Customer_Name, c.Email, c.Phone,
                       v.Name as VenueName, v.Location
                FROM Invoice i
                JOIN Event e ON i.EventID = e.EventID
                JOIN Customer c ON i.CustomerID = c.CustomerID
                LEFT JOIN Venue v ON e.VenueID = v.VenueID
                WHERE i.InvoiceID = ?
                """;

            Map<String, Object> invoice = jdbcTemplate.queryForMap(sql, invoiceId);

            // NO DATE CONVERSION - use raw dates

            // Get vendor details for this event
            String vendorsSql = """
                SELECT ev.ServiceType, v.VendorName, v.Price
                FROM EventVendor ev
                JOIN Vendor v ON ev.VendorID = v.VendorID
                WHERE ev.EventID = ?
                ORDER BY ev.ServiceType
                """;

            Integer eventId = (Integer) invoice.get("EventID");
            List<Map<String, Object>> vendors = jdbcTemplate.queryForList(vendorsSql, eventId);

            model.addAttribute("invoice", invoice);
            model.addAttribute("vendors", vendors);
            model.addAttribute("pageTitle", "Invoice #" + invoiceId);

            return "finance/invoice-details";

        } catch (Exception e) {
            return "redirect:/finance/invoices?error=invoice_not_found";
        }
    }

    // Helper method to load finance statistics
    private void loadFinanceStats(Model model) {
        try {
            // Total revenue
            String totalRevenueSql = "SELECT SUM(Total_Pay) FROM Invoice WHERE PaymentStatus = 'Received'";
            BigDecimal totalRevenue = jdbcTemplate.queryForObject(totalRevenueSql, BigDecimal.class);
            model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

            // Pending payments
            String pendingSql = "SELECT SUM(Total_Pay) FROM Invoice WHERE PaymentStatus = 'Pending'";
            BigDecimal pendingAmount = jdbcTemplate.queryForObject(pendingSql, BigDecimal.class);
            model.addAttribute("pendingAmount", pendingAmount != null ? pendingAmount : BigDecimal.ZERO);

            // Invoice counts
            String totalInvoicesSql = "SELECT COUNT(*) FROM Invoice";
            Integer totalInvoices = jdbcTemplate.queryForObject(totalInvoicesSql, Integer.class);
            model.addAttribute("totalInvoices", totalInvoices != null ? totalInvoices : 0);

            String pendingInvoicesSql = "SELECT COUNT(*) FROM Invoice WHERE PaymentStatus = 'Pending'";
            Integer pendingInvoices = jdbcTemplate.queryForObject(pendingInvoicesSql, Integer.class);
            model.addAttribute("pendingInvoices", pendingInvoices != null ? pendingInvoices : 0);

            // Recent invoices
            String recentInvoicesSql = """
                SELECT TOP 5 i.*, c.Customer_Name, e.EventType 
                FROM Invoice i
                JOIN Customer c ON i.CustomerID = c.CustomerID
                JOIN Event e ON i.EventID = e.EventID
                ORDER BY i.IssueDate DESC
                """;
            List<Map<String, Object>> recentInvoices = jdbcTemplate.queryForList(recentInvoicesSql);
            model.addAttribute("recentInvoices", recentInvoices);

        } catch (Exception e) {
            model.addAttribute("totalRevenue", BigDecimal.ZERO);
            model.addAttribute("pendingAmount", BigDecimal.ZERO);
            model.addAttribute("totalInvoices", 0);
            model.addAttribute("pendingInvoices", 0);
            model.addAttribute("recentInvoices", List.of());
        }
    }
}
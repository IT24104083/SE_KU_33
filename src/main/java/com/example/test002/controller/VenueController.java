package com.example.test002.controller;

import com.example.test002.model.Venue;
import com.example.test002.model.Inquiry;
import com.example.test002.service.VenueService;
import com.example.test002.service.InquiryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class VenueController {

    @Autowired
    private VenueService venueService;

    @Autowired
    private InquiryService inquiryService;

    // Home Page - UPDATED: Added eventDate parameter
    @GetMapping("/")
    public String showBookingHome(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double venueCost,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) String eventDate, // NEW: Add event date parameter
            Model model) {

        List<Venue> availableVenues;
        if ((location == null || location.isEmpty()) && venueCost == null && capacity == null && eventDate == null) {
            availableVenues = venueService.getAvailableVenues();
        } else {
            // Use date-based search if date provided, otherwise use basic search
            if (eventDate != null && !eventDate.isEmpty()) {
                availableVenues = venueService.searchVenues(location, venueCost, capacity, eventDate);
            } else {
                // Fallback to basic search without date filtering
                availableVenues = venueService.searchVenuesBasic(location, venueCost, capacity);
            }
        }

        model.addAttribute("availableVenues", availableVenues);
        model.addAttribute("location", location);
        model.addAttribute("venueCost", venueCost);
        model.addAttribute("capacity", capacity);
        model.addAttribute("eventDate", eventDate); // Add eventDate to model

        // Add inquiries for the Booking Requests tab
        List<Inquiry> inquiries = inquiryService.getAllInquiries();
        model.addAttribute("inquiries", inquiries);

        // Add all venues for the Update Hotel tab
        List<Venue> allVenues = venueService.getAllVenues();
        model.addAttribute("venues", allVenues);

        return "booking/index";
    }

    // Available Hotels with filters - UPDATED: Added eventDate parameter
    @GetMapping("/available-hotels")
    public String availableHotelsPage(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double venueCost,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) String eventDate, // NEW: Add event date parameter
            Model model) {

        List<Venue> venues;

        if ((location == null || location.isEmpty()) && venueCost == null && capacity == null && eventDate == null) {
            venues = venueService.getAvailableVenues();
        } else {
            // Use date-based search if date provided, otherwise use basic search
            if (eventDate != null && !eventDate.isEmpty()) {
                venues = venueService.searchVenues(location, venueCost, capacity, eventDate);
            } else {
                // Fallback to basic search without date filtering
                venues = venueService.searchVenuesBasic(location, venueCost, capacity);
            }
        }

        model.addAttribute("availableVenues", venues);
        model.addAttribute("location", location);
        model.addAttribute("venueCost", venueCost);
        model.addAttribute("capacity", capacity);
        model.addAttribute("eventDate", eventDate); // Add eventDate to model

        return "booking/available-hotels :: available-hotels";
    }

    // Booking Requests Page with DB integration
    @GetMapping("/booking-requests")
    public String bookingRequestsPage(Model model) {
        List<Inquiry> inquiries = inquiryService.getAllInquiries()
                .stream()
                .sorted((i1, i2) -> i1.getInquiryID().compareTo(i2.getInquiryID()))
                .toList();
        model.addAttribute("inquiries", inquiries);
        return "booking/booking-requests";
    }

    // Update Hotel Page - Load with all venues
    @GetMapping("/update-hotel")
    public String updateHotelPage(Model model) {
        List<Venue> allVenues = venueService.getAllVenues();
        model.addAttribute("venues", allVenues);
        return "booking/update-hotel :: update-hotel";
    }

    // Get venue details by ID (AJAX endpoint)
    @GetMapping("/get-venue/{venueId}")
    @ResponseBody
    public Venue getVenueById(@PathVariable Integer venueId) {
        return venueService.getVenueById(venueId);
    }

    // Update venue details
    @PostMapping("/update-hotel")
    public String updateHotelDetails(
            @RequestParam("hotelID") Integer venueId,
            @RequestParam("hotelName") String name,
            @RequestParam("location") String location,
            @RequestParam("venueCost") BigDecimal venueCost,
            @RequestParam("capacity") Integer capacity,
            RedirectAttributes redirectAttributes) {

        try {
            // Update the venue in the database
            venueService.updateVenue(venueId, name, location, venueCost, capacity);

            // Add flash attributes (these will be available only for the next request)
            redirectAttributes.addFlashAttribute("successMessage", "Hotel details updated successfully!");
            redirectAttributes.addFlashAttribute("showUpdateTab", true);

        } catch (Exception e) {
            // Add error message if update fails
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update hotel details. Please try again.");
            redirectAttributes.addFlashAttribute("showUpdateTab", true);
        }

        return "redirect:/";
    }

    // Booking History Page
    @GetMapping("/booking-history")
    public String bookingHistoryPage(Model model) {
        List<Inquiry> inquiries = inquiryService.getAllInquiries();
        model.addAttribute("inquiries", inquiries);
        return "booking/booking-history :: booking-history";
    }
}
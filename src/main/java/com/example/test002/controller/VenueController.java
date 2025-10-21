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

    // Home Page
    @GetMapping("/")
    public String showBookingHome(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double venueCost,
            @RequestParam(required = false) Integer capacity,
            Model model) {

        List<Venue> availableVenues;
        boolean isSearchPerformed = false;

        if ((location == null || location.isEmpty()) && venueCost == null && capacity == null) {
            availableVenues = venueService.getAvailableVenues();
        } else {
            availableVenues = venueService.searchVenues(location, venueCost, capacity);
            isSearchPerformed = true;
        }

        model.addAttribute("availableVenues", availableVenues);
        model.addAttribute("location", location);
        model.addAttribute("venueCost", venueCost);
        model.addAttribute("capacity", capacity);
        model.addAttribute("isSearchPerformed", isSearchPerformed);
        model.addAttribute("noResultsFound", isSearchPerformed && availableVenues.isEmpty());

        // Add inquiries for the Booking Requests tab
        List<Inquiry> inquiries = inquiryService.getAllInquiries();
        model.addAttribute("inquiries", inquiries);

        // Add all venues for the Update Hotel tab
        List<Venue> allVenues = venueService.getAllVenues();
        model.addAttribute("venues", allVenues);

        return "booking/index";
    }

    // Add Hotel - POST endpoint
    @PostMapping("/add-hotel")
    public String addHotel(
            @RequestParam("hotelName") String name,
            @RequestParam("location") String location,
            @RequestParam("venueCost") BigDecimal venueCost,
            @RequestParam("capacity") Integer capacity,
            @RequestParam("availability") String availability,
            RedirectAttributes redirectAttributes) {

        try {
            // Create new venue object
            Venue newVenue = new Venue();
            newVenue.setName(name);
            newVenue.setLocation(location);
            newVenue.setVenueCost(venueCost);
            newVenue.setCapacity(capacity);
            newVenue.setAvailability(availability);

            // Save to database
            venueService.createVenue(newVenue);

            // Add success message
            redirectAttributes.addFlashAttribute("addHotelSuccess",
                    "Hotel '" + name + "' has been added successfully!");

        } catch (Exception e) {
            // Add error message if creation fails
            redirectAttributes.addFlashAttribute("addHotelError",
                    "Failed to add hotel. Please try again. Error: " + e.getMessage());
        }

        return "redirect:/";
    }

    // Available Hotels with filters
    @GetMapping("/available-hotels")
    public String availableHotelsPage(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double venueCost,
            @RequestParam(required = false) Integer capacity,
            Model model) {

        List<Venue> venues;
        boolean isSearchPerformed = false;

        if ((location == null || location.isEmpty()) && venueCost == null && capacity == null) {
            venues = venueService.getAvailableVenues();
        } else {
            venues = venueService.searchVenues(location, venueCost, capacity);
            isSearchPerformed = true;
        }

        model.addAttribute("availableVenues", venues);
        model.addAttribute("location", location);
        model.addAttribute("venueCost", venueCost);
        model.addAttribute("capacity", capacity);
        model.addAttribute("isSearchPerformed", isSearchPerformed);
        model.addAttribute("noResultsFound", isSearchPerformed && venues.isEmpty());

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
            @RequestParam("availability") String availability,
            RedirectAttributes redirectAttributes) {

        try {
            // Update other fields
            venueService.updateVenue(venueId, name, location, venueCost, capacity);
            // Persist availability (uses existing JDBC method)
            venueService.updateVenueAvailability(venueId, availability);

            redirectAttributes.addFlashAttribute("successMessage", "Hotel details updated successfully!");
            redirectAttributes.addFlashAttribute("showUpdateTab", true);

        } catch (Exception e) {
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
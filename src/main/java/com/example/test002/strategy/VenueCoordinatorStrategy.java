package com.example.test002.strategy;

import org.springframework.stereotype.Component;

@Component
public class VenueCoordinatorStrategy implements LoginStrategy {

    @Override
    public String getRedirectUrl() {
        return "redirect:/venue/dashboard?login=success";
    }

    @Override
    public String getRoleName() {
        return "VenueCoordinator";
    }
}
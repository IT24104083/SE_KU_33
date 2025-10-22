package com.example.test002.strategy;

import com.example.test002.strategy.LoginStrategy;
import org.springframework.stereotype.Component;

@Component
public class VenueCoordinatorStrategy implements LoginStrategy {
    @Override
    public String getRedirectUrl() {
        return "redirect:/?login=success";  // Points to your VenueController's root mapping
    }

    @Override
    public String getRoleName() {
        return "VenueCoordinator";
    }
}
package com.example.test002.strategy;

import org.springframework.stereotype.Component;

@Component
public class EventCoordinatorStrategy implements LoginStrategy {

    @Override
    public String getRedirectUrl() {
        return "redirect:/event-coordinator/dashboard?login=success";
    }

    @Override
    public String getRoleName() {
        return "EventCoordinator";
    }
}
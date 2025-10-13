package com.example.test002.strategy;

import org.springframework.stereotype.Component;

@Component
public class VendorCoordinatorStrategy implements LoginStrategy {

    @Override
    public String getRedirectUrl() {
        return "redirect:/vendors/dashboard?login=success";
    }

    @Override
    public String getRoleName() {
        return "VendorCoordinator";
    }
}
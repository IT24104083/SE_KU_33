package com.example.test002.strategy;

import org.springframework.stereotype.Component;

@Component
public class CustomerSupportOfficerStrategy implements LoginStrategy {

    @Override
    public String getRedirectUrl() {
        return "redirect:/support/dashboard?login=success";
    }

    @Override
    public String getRoleName() {
        return "CustomerSupportOfficer";
    }
}
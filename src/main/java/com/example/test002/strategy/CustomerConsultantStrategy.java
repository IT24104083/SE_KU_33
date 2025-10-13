package com.example.test002.strategy;

import org.springframework.stereotype.Component;

@Component
public class CustomerConsultantStrategy implements LoginStrategy {

    @Override
    public String getRedirectUrl() {
        return "redirect:/consultant/dashboard?login=success";
    }

    @Override
    public String getRoleName() {
        return "CustomerConsultant";
    }
}
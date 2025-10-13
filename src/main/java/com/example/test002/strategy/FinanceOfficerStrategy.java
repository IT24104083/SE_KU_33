package com.example.test002.strategy;

import org.springframework.stereotype.Component;

@Component
public class FinanceOfficerStrategy implements LoginStrategy {

    @Override
    public String getRedirectUrl() {
        return "redirect:/finance/dashboard?login=success";
    }

    @Override
    public String getRoleName() {
        return "FinanceOfficer";
    }
}
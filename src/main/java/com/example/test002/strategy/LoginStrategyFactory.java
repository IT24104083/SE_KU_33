package com.example.test002.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory class that provides the appropriate login strategy based on user role
 * This eliminates the need for switch statements in the AuthController
 */
@Service
public class LoginStrategyFactory {

    private final Map<String, LoginStrategy> strategies;

    /**
     * Constructor that automatically injects all LoginStrategy implementations
     * and maps them by their role names
     */
    @Autowired
    public LoginStrategyFactory(List<LoginStrategy> strategyList) {
        strategies = new HashMap<>();
        for (LoginStrategy strategy : strategyList) {
            strategies.put(strategy.getRoleName(), strategy);
        }
    }

    /**
     * Returns the appropriate strategy for the given user role
     * @param role the user role from the database
     * @return the LoginStrategy implementation for that role
     * @throws IllegalArgumentException if the role is not supported
     */
    public LoginStrategy getStrategy(String role) {
        LoginStrategy strategy = strategies.get(role);
        if (strategy == null) {
            throw new IllegalArgumentException("Invalid user role: " + role);
        }
        return strategy;
    }

    /**
     * Checks if the factory supports a given role
     * @param role the user role to check
     * @return true if the role is supported, false otherwise
     */
    public boolean supportsRole(String role) {
        return strategies.containsKey(role);
    }

    /**
     * Returns all supported roles
     * @return array of supported role names
     */
    public String[] getSupportedRoles() {
        return strategies.keySet().toArray(new String[0]);
    }
}
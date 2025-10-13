package com.example.test002.strategy;

/**
 * Strategy interface for handling role-based login redirections
 * This replaces the switch statement in AuthController with a polymorphic solution
 */
public interface LoginStrategy {

    /**
     * Returns the redirect URL for the specific user role after successful login
     * @return the redirect URL string
     */
    String getRedirectUrl();

    /**
     * Returns the role name that this strategy handles
     * @return the role name as String
     */
    String getRoleName();

    /**
     * Optional: Perform any role-specific post-login actions
     * Default implementation does nothing
     */
    default void performPostLoginActions() {
        // Can be overridden by concrete strategies for role-specific initialization
    }
}
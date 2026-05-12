package com.paymentproject.payment.Model;

/**
 * Role Enumeration
 * 
 * Defines the possible roles in the banking system:
 * - ADMIN: Has full access to all system features including user management
 * - USER: Has access to basic banking features like money transfers
 * 
 * This enum is used for:
 * - Role-based access control (RBAC)
 * - Security configuration
 * - User permission management
 */
public enum Role {
    /**
     * Administrator role with full system access
     */
    ADMIN("ADMIN"),

    /**
     * Standard user role with basic banking access
     */
    USER("USER");

    /**
     * String representation of the role
     */
    private String value;

    /**
     * Constructor for Role enum
     * 
     * @param value String representation of the role
     */
    Role(String value) {
        this.value = value;
    }

    /**
     * Get the string value of the role
     * 
     * @return String representation of the role
     */
    public String getValue() {
        return value;
    }

    /**
     * Override toString to return the role's string value
     * 
     * @return String representation of the role
     */
    @Override
    public String toString() {
        return value;
    }
}

package com.paymentproject.payment.dto;

import java.util.Set;

import com.paymentproject.payment.Model.Role;

import lombok.Data;

/**
 * Data Transfer Object for Customer Registration
 * 
 * This DTO is specifically used for the registration process:
 * - Captures only the fields needed for creating a new customer
 * - Separates registration concerns from general customer data
 * - Provides a clear contract for the registration API
 * 
 * Uses Lombok @Data for automatic getter/setter generation
 */
@Data
public class CustomerRegistrationDTO {
    /**
     * Customer's desired username/name
     */
    private String customerName;

    /**
     * Customer's password (will be hashed before storage)
     */
    private String password;

    /**
     * Initial money for the customer (optional, can be set to 0 by default)
     */
    private int money;

    private Set<Role> role; // Optional: Set of roles to assign during registration (e.g., USER)

}

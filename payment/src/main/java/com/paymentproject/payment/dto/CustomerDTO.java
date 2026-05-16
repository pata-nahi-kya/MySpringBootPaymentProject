package com.paymentproject.payment.dto;

import lombok.Data;
import java.util.Set;

import com.paymentproject.payment.Model.Role;

/**
 * Data Transfer Object for Customer Information
 * 
 * This DTO is used for transferring customer data between layers while:
 * - Hiding sensitive information (like passwords)
 * - Including only necessary fields for client communication
 * - Providing a clean API contract
 * 
 * Uses Lombok @Data for automatic getter/setter generation
 */
@Data
public class CustomerDTO {
    /**
     * Unique identifier of the customer
     */
    private int id;

    /**
     * Customer's username/name
     */
    private String customerName;

    /**
     * Customer's account balance
     */
    private double money;

    /**
     * Set of roles assigned to the customer
     */
    private Set<Role> role;
}
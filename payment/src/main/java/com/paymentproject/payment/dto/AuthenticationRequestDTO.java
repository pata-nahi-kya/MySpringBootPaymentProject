package com.paymentproject.payment.dto;

import lombok.Data;

/**
 * Data Transfer Object for Authentication Requests
 * 
 * This DTO is used for:
 * - Capturing login credentials
 * - Processing authentication requests
 * - Separating authentication concerns from business logic
 * 
 * Uses Lombok @Data for automatic getter/setter generation
 */
@Data
public class AuthenticationRequestDTO {
    /**
     * Username for authentication
     */
    private String CustomerName;

    /**
     * Password for authentication
     */
    private String password;
}
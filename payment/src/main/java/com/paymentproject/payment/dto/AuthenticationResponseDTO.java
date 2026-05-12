package com.paymentproject.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Authentication Responses
 * 
 * This DTO is used for:
 * - Returning JWT tokens after successful authentication
 * - Providing authentication status messages
 * - Encapsulating authentication response data
 * 
 * Uses Lombok annotations:
 * - @Data for getters/setters
 * - @AllArgsConstructor for full constructor
 * - @NoArgsConstructor for default constructor
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponseDTO {
    /**
     * JWT token for authenticated session
     */
    private String token;

    /**
     * Status message about authentication result
     */
    private String message;
}
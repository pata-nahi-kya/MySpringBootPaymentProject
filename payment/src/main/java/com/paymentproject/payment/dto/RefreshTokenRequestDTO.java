package com.paymentproject.payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for endpoints that receive a refresh token in the request body.
 *
 * Refresh tokens must never be sent as query parameters because:
 * 1. Query parameters appear in server access logs.
 * 2. They are stored in browser history.
 * 3. They can be leaked via the HTTP Referer header.
 *
 * Sending them in the JSON body (over HTTPS) keeps them out of logs and
 * history.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequestDTO {
    private String refreshToken;
}

package com.paymentproject.payment.ControllerLayer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.paymentproject.payment.ServiceStructureImplementation.JWTService;
import com.paymentproject.payment.ServiceStructureImplementation.RefreshTokenImplementation;
import com.paymentproject.payment.dto.AuthenticationRequestDTO;
import com.paymentproject.payment.dto.AuthenticationResponseDTO;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/bank/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private RefreshTokenImplementation refreshTokenService;

    @PostMapping("/loginandgettoken")
    public ResponseEntity<AuthenticationResponseDTO> login(@RequestBody AuthenticationRequestDTO authRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getCustomerName(), authRequest.getPassword()));

        if (authentication.isAuthenticated()) {
            String accessToken = jwtService.generateToken(authRequest.getCustomerName());
            String refreshToken = refreshTokenService.createRefreshToken(authRequest.getCustomerName());

            // FIXED: Build a secure, HttpOnly, SameSite cookie wrapper
            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true) // Stops JavaScript from reading the cookie
                    .secure(true) // Set to true in production when using HTTPS
                    .path("/") // Accessible across the whole domain API path
                    .maxAge(7 * 24 * 60 * 60) // Matches the 7-day expiration time
                    .sameSite("Lax") // Prevents Cross-Site Request Forgery (CSRF)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    // Only return the short-lived access token in the JSON body
                    .body(new AuthenticationResponseDTO(accessToken));
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponseDTO> refresh(HttpServletRequest request) {
        String token = null;

        // FIXED: Extract the token safely out of the incoming HTTP Cookie array mapping
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is missing");
        }

        String username = refreshTokenService.validateRefreshToken(token);
        if (username == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
        }

        String newAccessToken = jwtService.generateToken(username);
        String refreshToken = refreshTokenService.createRefreshToken(username);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true) // Stops JavaScript from reading the cookie
                .secure(true) // Set to true in production when using HTTPS
                .path("/") // Accessible across the whole domain API path
                .maxAge(7 * 24 * 60 * 60) // Matches the 7-day expiration time
                .sameSite("Lax") // Prevents Cross-Site Request Forgery (CSRF)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                // Only return the short-lived access token in the JSON body
                .body(new AuthenticationResponseDTO(newAccessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Clear the cookie out of the browser by setting its maxAge to 0
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}

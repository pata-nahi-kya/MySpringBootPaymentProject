package com.paymentproject.payment.ControllerLayer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.paymentproject.payment.ServiceStructure.RefreshTokenStructure;
import com.paymentproject.payment.ServiceStructureImplementation.JWTService;
import com.paymentproject.payment.dto.AuthenticationRequestDTO;
import com.paymentproject.payment.dto.AuthenticationResponseDTO;



@RequestMapping("/bank/auth")
@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
@RestController
public class AuthController {

    /**
     * Authentication manager used for credential verification
     */
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JWTService jwtService;

    @Autowired
    RefreshTokenStructure refreshTokenService;

    /**
     * Generate JWT token for user authentication
     * 
     * @return Authentication response containing JWT token
     */
    @PostMapping("/loginandgettoken")
    public AuthenticationResponseDTO getTokenOfJWT(AuthenticationRequestDTO authRequest) {
        String username = authRequest.getCustomerName();
        String password = authRequest.getPassword();
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            if (authentication.isAuthenticated()) {
                String token = jwtService.generateToken(username);
                String refreshToken = refreshTokenService.createRefreshToken(username);
                // Cookie cookie = new Cookie("refreshToken", refreshToken);
                // cookie.setHttpOnly(true);
                // cookie.setSecure(true);
                // cookie.setPath("/bank/auth/refresh");
                // cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
                // response.addCookie(cookie);
                return new AuthenticationResponseDTO(token, refreshToken, "Authentication successful");
            }
        } catch (AuthenticationException ex) {
            return new AuthenticationResponseDTO(null, null, "Invalid credentials");
        }
        return new AuthenticationResponseDTO(null, null, "Authentication failed");
    }

    @PostMapping("/logout")
    public String logout(@RequestParam String refreshToken) {

        refreshTokenService.deleteRefreshToken(refreshToken);

        return "Logged out";
    }

    @PostMapping("/refresh")
    public AuthenticationResponseDTO refresh(@RequestParam String refreshToken) {
        String username = refreshTokenService.validateRefreshToken(refreshToken);
        // checking if the refresh token is valid and belongs to a user
        if (username == null) {
            return new AuthenticationResponseDTO(null, null, "Invalid refresh token");
        }

        // checking if the refresh token is expired
        if (refreshTokenService.isRefreshTokenExpired(refreshToken)) {
            return new AuthenticationResponseDTO(null, null, "Refresh token has expired");
        }

        refreshTokenService.deleteRefreshToken(refreshToken);
        String newRefreshToken = refreshTokenService.createRefreshToken(username);
        String newAccessToken = jwtService.generateToken(username);
        return new AuthenticationResponseDTO(newAccessToken, newRefreshToken, "Token refreshed successfully");

    }

}

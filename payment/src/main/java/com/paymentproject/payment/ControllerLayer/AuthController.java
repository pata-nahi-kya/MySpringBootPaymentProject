package com.paymentproject.payment.ControllerLayer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paymentproject.payment.ServiceStructure.RefreshTokenStructure;
import com.paymentproject.payment.ServiceStructureImplementation.JWTService;
import com.paymentproject.payment.dto.AuthenticationRequestDTO;
import com.paymentproject.payment.dto.AuthenticationResponseDTO;
import com.paymentproject.payment.dto.RefreshTokenRequestDTO;

/**
 * Authentication Controller
 *
 * Handles login, logout, and token refresh. This controller must NOT have any
 * class-level @PreAuthorize because the login endpoint must be publicly
 * accessible. Authorization for individual endpoints is handled either by
 * SecurityConfig or method-level annotations where needed.
 *
 * Security note: refresh tokens are sent in the request body rather than as
 * query parameters to prevent them from appearing in server access logs.
 */
@RestController
@RequestMapping("/bank/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private RefreshTokenStructure refreshTokenService;

    /**
     * Authenticate user credentials and return a JWT access token plus a refresh
     * token.
     *
     * Bug fixed: @RequestBody was missing, so Spring never deserialized the JSON
     * body and the DTO fields were always null, causing authentication to fail.
     *
     * Bug fixed: class-level @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
     * was blocking this endpoint for unauthenticated users — a chicken-and-egg
     * problem that made login impossible without already being logged in.
     *
     * @param authRequest DTO containing customerName and password
     * @return ResponseEntity with JWT tokens on success, or 401 on failure
     */
    @PostMapping("/loginandgettoken")
    public ResponseEntity<AuthenticationResponseDTO> getTokenOfJWT(
            @RequestBody AuthenticationRequestDTO authRequest) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getCustomerName(),
                            authRequest.getPassword()));

            if (authentication.isAuthenticated()) {
                String token = jwtService.generateToken(authRequest.getCustomerName());
                String refreshToken = refreshTokenService.createRefreshToken(authRequest.getCustomerName());
                return ResponseEntity.ok(
                        new AuthenticationResponseDTO(token, refreshToken, "Authentication successful"));
            }

        } catch (AuthenticationException ex) {
            // Do not leak details about why authentication failed
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthenticationResponseDTO(null, null, "Invalid credentials"));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthenticationResponseDTO(null, null, "Authentication failed"));
    }

    /**
     * Invalidate the given refresh token (logout).
     *
     * Bug fixed: token was a @RequestParam (query string), which means it appears
     * in server access logs and browser history. It is now sent in the request
     * body.
     *
     * @param request DTO containing the refresh token
     * @return confirmation message
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody RefreshTokenRequestDTO request) {
        refreshTokenService.deleteRefreshToken(request.getRefreshToken());
        return ResponseEntity.ok("Logged out successfully");
    }

    /**
     * Issue a new access token and rotate the refresh token.
     *
     * Refresh token rotation means the old token is deleted and a brand-new one is
     * issued on every refresh call. This limits the damage if a refresh token is
     * stolen: after one use it is gone.
     *
     * Bug fixed: same query-param exposure issue as logout.
     *
     * @param request DTO containing the refresh token
     * @return new access token and new refresh token, or 401 if invalid/expired
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponseDTO> refresh(@RequestBody RefreshTokenRequestDTO request) {
        String refreshToken = request.getRefreshToken();

        if (refreshTokenService.isRefreshTokenExpired(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthenticationResponseDTO(null, null, "Refresh token has expired"));
        }

        String username = refreshTokenService.validateRefreshToken(refreshToken);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthenticationResponseDTO(null, null, "Invalid refresh token"));
        }

        // Rotate: delete old, issue new
        refreshTokenService.deleteRefreshToken(refreshToken);
        String newRefreshToken = refreshTokenService.createRefreshToken(username);
        String newAccessToken = jwtService.generateToken(username);

        return ResponseEntity.ok(
                new AuthenticationResponseDTO(newAccessToken, newRefreshToken, "Token refreshed successfully"));
    }
}

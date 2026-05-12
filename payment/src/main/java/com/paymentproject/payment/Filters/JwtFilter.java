package com.paymentproject.payment.Filters;

import com.paymentproject.payment.ServiceStructureImplementation.JWTService;
import com.paymentproject.payment.userDetailService.CustomUserDetailService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter
 * 
 * This filter intercepts all HTTP requests and:
 * 1. Extracts JWT token from Authorization header
 * 2. Validates the token
 * 3. Loads user details if token is valid
 * 4. Sets up Spring Security context
 * 
 * Security features:
 * - Executes once per request
 * - Validates token format and signature
 * - Checks token expiration
 * - Sets up proper authentication context
 * 
 * Extends OncePerRequestFilter to ensure single execution per request
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    /**
     * Service for JWT operations (validation, extraction)
     */
    @Autowired
    private JWTService jwtService;

    /**
     * Application context for accessing beans
     * Used to get UserDetailsService dynamically
     */
    @Autowired
    private CustomUserDetailService userDetailsService;

    /**
     * Main filter method that processes each request
     * 
     * Process flow:
     * 1. Extract JWT from Authorization header
     * 2. Extract username from token
     * 3. Validate token and load user details
     * 4. Set up security context if token is valid
     * 
     * @param request     HTTP request
     * @param response    HTTP response
     * @param filterChain Filter chain to execute
     * @throws ServletException if filter processing fails
     * @throws IOException      if I/O error occurs
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            username = jwtService.extractUserName(token);
        } else if (request.getParameter("token") != null) {
            // For WebSocket connections via SockJS query parameters
            token = request.getParameter("token");
            username = jwtService.extractUserName(token);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtService.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                        null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}

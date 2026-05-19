package com.paymentproject.payment.Filters;

import com.paymentproject.payment.ServiceStructureImplementation.JWTService;
import com.paymentproject.payment.userDetailService.CustomUserDetailService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

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

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private CustomUserDetailService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
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

            // FIXED: This is the ONLY place where a successful filter execution proceeds
            // forward
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            logger.warn("JWT validation failed: Access Token has expired -> " + e.getMessage());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // HTTP 401
            response.setContentType("application/json");
            response.getWriter()
                    .write("{\"error\": \"Access Token Expired\", \"message\": \"" + e.getMessage() + "\"}");

            return; // Stops execution instantly. Does not fall through.

        } catch (JwtException e) {
            logger.error("JWT validation failed: Invalid Token format -> " + e.getMessage());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // HTTP 401
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\": \"Invalid Token\", \"message\": \"The provided token signature is corrupted.\"}");

            return; // Stops execution instantly.

        } catch (Exception e) {
            logger.error("Unexpected error in JwtFilter: ", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // HTTP 500
            return;
        }

        // FIXED: The duplicate filterChain.doFilter() call that was down here has been
        // deleted!
    }
}

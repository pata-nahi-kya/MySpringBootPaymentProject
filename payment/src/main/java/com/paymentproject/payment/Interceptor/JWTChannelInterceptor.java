package com.paymentproject.payment.Interceptor;



import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.paymentproject.payment.ServiceStructureImplementation.JWTService;
import com.paymentproject.payment.userDetailService.CustomUserDetailService;

import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

@Component
public class JWTChannelInterceptor implements ChannelInterceptor {

    private JWTService jwtService;
    private CustomUserDetailService userDetailsService;

    public JWTChannelInterceptor(JWTService jwtService, CustomUserDetailService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message,
                StompHeaderAccessor.class);

        // Only process authentication during initial STOMP CONNECT
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtService.extractUserName(token);

                try {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (jwtService.validateToken(token, userDetails)) {
                        // token is valid, proceed with authentication
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());

                        accessor.setUser(authentication);
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        return message; // Allow the CONNECT message to proceed
                    }

                    throw new IllegalArgumentException("Invalid JWT token");

                } catch (Exception e) {
                    // Token is invalid. Do not throw exceptions, as it will crash the WS
                    // connection.
                    // The user will be treated as unauthenticated.
                    System.err.println("Cannot authenticate STOMP message: " + e.getMessage());
                }
            }
        }

        return message;
    }
}
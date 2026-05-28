package com.paymentproject.payment.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;



@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // @Autowired
    // private JWTService jwtService;

    // @Autowired
    // private CustomUserDetailService userDetailsService;

    @Autowired
    private com.paymentproject.payment.Interceptor.JWTChannelInterceptor jwtChannelInterceptor;

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // Register the /ws endpoint for WebSocket communication
        // Allow all origins for simplicity (not recommended for production)
        // step by step connection process to chatting
        // step 1 : client will connect to /ws endpoint using SockJS
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {

        // Enable a simple in-memory message broker and set the destination prefix for
        // messages
        // Removed /user because it is a special prefix handled by
        // UserDestinationMessageHandler, not the simple broker.

        registry.enableSimpleBroker("/topic", "/queue");

        // Set the application destination prefix for messages that will be routed to
        // message-handling methods in the controller
        // step 2 : client will send message to /app/hello endpoint and then it will be
        // routed to controller method with @MessageMapping("/hello")
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(
            @NonNull ChannelRegistration registration) {

        registration.interceptors(jwtChannelInterceptor);
    }

}

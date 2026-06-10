package com.paymentproject.payment.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.paymentproject.payment.Filters.JwtFilter;
import com.paymentproject.payment.Model.Role;
import com.paymentproject.payment.userDetailService.CustomUserDetailService;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity   
public class SecurityConfig {

    @Autowired
    private CustomUserDetailService customUserDetailsService;

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // FIXED: Added active CORS filter tracking rules to link HttpOnly Cookies
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth

                // Static pages — no authentication required
                .requestMatchers(
                    "/", "/login",
                    "/index.html", "/dashboard.html", "/makePayment.html",
                    "/chatsection.html", "/register.html",
                    "/ws/**"
                ).permitAll()

                // Swagger UI — no authentication required
                .requestMatchers(
                    "/swagger-ui/**", "/swagger-ui.html",
                    "/swagger-ui/index.html", "/v3/api-docs/**",
                    "/actuator/**"
                ).permitAll()

                // Authentication endpoints: login, token refresh, and logout are managed here
                .requestMatchers(
                    "/bank/auth/loginandgettoken",
                    "/bank/auth/refresh",
                    "/bank/auth/logout",
                    "/bank/auth/register/initiate",
                    "/bank/auth/register/verify"
                ).permitAll()

                // Admin endpoints — ADMIN role enforced at the filter chain level.
                .requestMatchers("/bank/admin/**").hasRole(Role.ADMIN.name())

                // User endpoints — USER or ADMIN role required
                .requestMatchers("/bank/user/**").hasAnyRole(Role.ADMIN.name(), Role.USER.name())

                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(customUserDetailsService);
        return provider;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * FIXED: Added explicit Cross-Origin configuration properties.
     * Instructs the browser to process incoming 'Set-Cookie' parameters securely.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Add your exact local/production frontend origin roots here
        configuration.setAllowedOrigins(List.of("http://localhost:5500", "http://127.0.0.1:5500", "http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        
        // CRITICAL: True flag permits transmitting secure HttpOnly cookies across origins
        configuration.setAllowCredentials(true); 

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

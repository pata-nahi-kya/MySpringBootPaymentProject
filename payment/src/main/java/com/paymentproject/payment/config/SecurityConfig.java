package com.paymentproject.payment.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.paymentproject.payment.Filters.JwtFilter;
import com.paymentproject.payment.Model.Role;
import com.paymentproject.payment.userDetailService.CustomUserDetailService;

/**
 * Security Configuration Class
 * 
 * This class configures Spring Security for the application, including:
 * - JWT-based authentication
 * - Role-based authorization
 * - Password encryption
 * - Security filter chain
 * - Session management
 * 
 * @Configuration marks this as a configuration class
 * @EnableWebSecurity enables Spring Security's web security support
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    /**
     * User Details Service for loading user-specific data
     */
    @Autowired
    CustomUserDetailService customUserDetailsService;

    /**
     * Custom JWT filter for token-based authentication
     */
    @Autowired
    JwtFilter jwtFilter;

    // this file is used to configure the security of the application
    // it is used to configure the security filter chain
    // it is used to configure the authentication provider
    // it is used to configure the user details service
    // it is used to configure the password encoder
    // it is used to configure the session management policy
    // it is used to configure the CSRF protection settings
    // it is used to configure the HTTP security

    /**
     * Configures the security filter chain for HTTP requests
     * 
     * This method defines:
     * - URL-based security rules
     * - Role-based access control
     * - JWT filter integration
     * - Session management policy
     * - CSRF protection settings
     * 
     * @param http HttpSecurity object to configure
     * @return Configured SecurityFilterChain
     * @throws Exception if configuration fails
     */

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - accessible without authentication
                        .requestMatchers("/", "/login", "/index.html", "/dashboard.html", "/makePayment.html",
                                "/chatsection.html", "/ws/**")
                        .permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/swagger-ui/index.html",
                                "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers("/bank/admin/createUser").permitAll()
                        .requestMatchers("/bank/auth/loginandgettoken/**").permitAll()

                        // Admin endpoints - require ADMIN role
                        .requestMatchers("/bank/admin/**").hasRole(Role.ADMIN.name())

                        // User endpoints - require USER or ADMIN role
                        .requestMatchers("/bank/user/**").hasAnyRole(Role.ADMIN.name(), Role.USER.name())

                        // All other requests must be authenticated
                        .anyRequest().authenticated()

                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        // JWT filter only applies to protected endpoints, not public ones
        // Public endpoints like "/" and "/login" don't need JWT validation

        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.authenticationProvider(AP());

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // Authentication Provider is used to decide which type of authentication we are
    // using ex Oauth or JWT or basic authentication
    // here we are using DaoAuthenticationProvider which is used to retrieve user
    // details from a database
    // we have to provide userDetailsService and passwordEncoder to the
    // DaoAuthenticationProvider
    // userDetailsService is used to load user details from database and
    // passwordEncoder is used to encode the password
    // we are using BCryptPasswordEncoder which is a strong hashing function to
    // encode the password

    // there is one more class namedn authentication manager which tells
    // authentication provider which to choose on the basis of given information
    /**
     * Configures the authentication provider for the application
     * 
     * This method sets up:
     * - DaoAuthenticationProvider for database-based authentication
     * - BCryptPasswordEncoder with strength 10 for password hashing
     * - Custom UserDetailsService for loading user data
     * 
     * The authentication provider is responsible for:
     * - Verifying user credentials
     * - Loading user details from the database
     * - Managing password encoding/verification
     * 
     * @return Configured AuthenticationProvider
     */
    @Bean
    public AuthenticationProvider AP() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(new BCryptPasswordEncoder(10)); // Strong password hashing
        provider.setUserDetailsService(customUserDetailsService); // Custom user details service
        return provider;
    }

}

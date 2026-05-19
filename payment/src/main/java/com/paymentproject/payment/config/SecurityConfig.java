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

import com.paymentproject.payment.Filters.JwtFilter;
import com.paymentproject.payment.Model.Role;
import com.paymentproject.payment.userDetailService.CustomUserDetailService;

/**
 * Security Configuration
 *
 * --- Bug fixed: /bank/auth/loginandgettoken was not in the permit list ---
 * The original config permitted "/bank/admin/createUser" (which should be
 * protected) but never permitted the login endpoint itself. Because
 * AuthController also had a class-level @PreAuthorize, unauthenticated users
 * could not reach the login endpoint at all, making login impossible.
 *
 * Fixed by:
 * 1. Permitting /bank/auth/loginandgettoken, /bank/auth/refresh publicly.
 *    Logout requires an authenticated user (you need to be logged in to log
 *    out), so it is intentionally left out of the permit list.
 * 2. Removing the public permit on /bank/admin/createUser — admin endpoints
 *    must require ADMIN role.
 *
 * --- Bug fixed: duplicate BCryptPasswordEncoder instantiation ---
 * BCryptPasswordEncoder was created inline in AP() and also as a field in
 * CustomerServiceImpl. It is now a @Bean here so it can be injected wherever
 * needed, ensuring a single instance with consistent configuration.
 *
 * --- Added: @EnableMethodSecurity ---
 * Required for @PreAuthorize annotations on controllers to take effect.
 * Without this, method-level security annotations are silently ignored.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // enables @PreAuthorize, @PostAuthorize, @Secured on methods
public class SecurityConfig {

    @Autowired
    private CustomUserDetailService customUserDetailsService;

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth

                // Static pages — no authentication required
                .requestMatchers(
                    "/", "/login",
                    "/index.html", "/dashboard.html", "/makePayment.html",
                    "/chatsection.html", "/register.html",
                    "/ws/**"
                ).permitAll()

                // Swagger UI — no authentication required (remove in production or protect)
                .requestMatchers(
                    "/swagger-ui/**", "/swagger-ui.html",
                    "/swagger-ui/index.html", "/v3/api-docs/**"
                ).permitAll()

                // Authentication endpoints: login and token refresh are public.
                // Logout is intentionally NOT here — it requires a valid session.
                .requestMatchers(
                    "/bank/auth/loginandgettoken",
                    "/bank/auth/refresh"
                ).permitAll()

                // Admin endpoints — ADMIN role enforced at the filter chain level.
                // Individual methods may add further @PreAuthorize constraints.
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

    /**
     * DaoAuthenticationProvider wires together:
     * - UserDetailsService (loads user record from DB by username)
     * - PasswordEncoder  (verifies the submitted password against the stored hash)
     *
     * Spring Security's AuthenticationManager delegates to this provider for
     * username/password authentication.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(customUserDetailsService);
        return provider;
    }

    /**
     * Password encoder bean — defined once, injected wherever needed.
     *
     * Strength 12 is the current industry recommendation (2024+). Strength 10 is
     * the Spring Security default and acceptable; 12 adds ~4x more hashing time,
     * which is negligible at login but meaningful for brute-force resistance.
     * Adjust based on your server's acceptable login latency.
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}

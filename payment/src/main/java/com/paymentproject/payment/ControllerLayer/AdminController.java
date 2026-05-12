package com.paymentproject.payment.ControllerLayer;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import com.paymentproject.payment.dto.CustomerDTO;
import com.paymentproject.payment.dto.CustomerMapper;
import com.paymentproject.payment.dto.CustomerRegistrationDTO;
import com.paymentproject.payment.dto.AuthenticationResponseDTO;
import com.paymentproject.payment.ServiceStructureImplementation.*;

/**
 * Admin Controller
 * 
 * This controller handles administrative operations in the banking system.
 * All endpoints in this controller require ADMIN role authentication.
 * 
 * Provides functionality for:
 * - User management (create, delete, view all)
 * - Authentication token generation
 * - System administration
 * 
 * @PreAuthorize ensures only users with ADMIN role can access these endpoints
 *               All responses use DTOs to ensure proper data encapsulation
 */
@RestController
@RequestMapping("/bank/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    /**
     * Service implementation for business logic operations
     */
    @Autowired
    ssImplementation ss;

    /**
     * Authentication manager used for credential verification
     */
    @Autowired
    AuthenticationManager authenticationManager;

    /**
     * Service for JWT token generation and validation
     */
    @Autowired
    JWTService sj;

    /**
     * Mapper for converting between entities and DTOs
     */
    @Autowired
    CustomerMapper customerMapper;

    /**
     * Welcome endpoint for admin panel
     * 
     * @return Welcome message for administrators
     */
    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome to the Admin Panel";
    }

    /**
     * Create a new user in the system
     * 
     * @param registrationDTO DTO containing new user registration information
     * @return Created user information as DTO
     */
    @PostMapping("/createUser")
    public CustomerDTO createUser(@RequestBody CustomerRegistrationDTO registrationDTO) {
        return customerMapper.toDto(ss.createUser(customerMapper.toEntity(registrationDTO)));
    }

    /**
     * Retrieve information about all users in the system
     * 
     * @return List of all users' information as DTOs
     */
    @GetMapping("/getAllUser")
    public List<CustomerDTO> getAllInfo() {
        return ss.getAllInfo().stream()
                .map(customerMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Delete a user from the system
     * 
     * @param id The ID of the user to delete
     */
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable int id) {
        ss.deleteUser(id);
    }

    /**
     * Generate JWT token for user authentication
     * 
     * @param username Username for token generation
     * @param password Password for verification (not used in current
     *                 implementation)
     * @return Authentication response containing JWT token
     */
    @PutMapping("/authenticate/{username}/{password}")
    public AuthenticationResponseDTO getTokenOfJWT(@PathVariable String username, @PathVariable String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            if (authentication.isAuthenticated()) {
                String token = sj.generateToken(username);
                return new AuthenticationResponseDTO(token, "Authentication successful");
            }
        } catch (AuthenticationException ex) {
            return new AuthenticationResponseDTO(null, "Invalid credentials");
        }
        return new AuthenticationResponseDTO(null, "Authentication failed");
    }

}

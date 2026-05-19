package com.paymentproject.payment.ControllerLayer;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;
import com.paymentproject.payment.dto.CustomerDTO;
import com.paymentproject.payment.dto.CustomerMapper;
import com.paymentproject.payment.dto.CustomerRegistrationDTO;

import com.paymentproject.payment.Model.CustomerInfo;
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
    CustomerServiceImpl customerService;

    /**
     * Service for JWT token generation and validation
     */

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
        return customerMapper.toDto(customerService.createUser(customerMapper.toEntity(registrationDTO)));
    }

    /**
     * Bulk add users (admin only)
     *
     * @param registrations List of registration DTOs
     * @return List of created users as DTOs
     */
    @PostMapping("/bulkAdd")
    public List<CustomerDTO> bulkAdd(@RequestBody List<CustomerRegistrationDTO> registrations) {
        List<CustomerInfo> entities = registrations.stream()
                .map(customerMapper::toEntity)
                .collect(Collectors.toList());

        java.util.List<com.paymentproject.payment.Model.CustomerInfo> saved = customerService
                .bulkAddCustomers(entities);

        return saved.stream().map(customerMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Retrieve information about all users in the system
     * 
     * @return List of all users' information as DTOs
     */
    @GetMapping("/getAllUser")
    public List<CustomerDTO> getAllInfo() {
        // let add pagination and sorting in future
        return customerService.getAllInfo().stream()
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
        customerService.deleteUser(id);
    }

}

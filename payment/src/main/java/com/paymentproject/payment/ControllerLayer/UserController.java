package com.paymentproject.payment.ControllerLayer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.paymentproject.payment.Model.CustomerInfo;
import com.paymentproject.payment.ServiceStructureImplementation.JWTService;
import com.paymentproject.payment.ServiceStructureImplementation.CustomerServiceImpl;
import com.paymentproject.payment.dto.CustomerDTO;
import com.paymentproject.payment.dto.CustomerMapper;
import com.paymentproject.payment.dto.TransferDTO;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * User Controller
 * 
 * This controller handles all user-related operations in the banking system.
 * It provides endpoints for:
 * - Money management (add/transfer)
 * - User profile management
 * - Account information retrieval
 * 
 * All endpoints in this controller require USER or ADMIN role authentication.
 * Data is transferred using DTOs to ensure security and proper encapsulation.
 */
@RestController
@RequestMapping("/bank/user")
public class UserController {

    /**
     * Service layer implementation for business logic
     */
    @Autowired
    CustomerServiceImpl customerService;

    @Autowired
    JWTService jwtService;

    /**
     * Mapper for converting between entities and DTOs
     */
    @Autowired
    CustomerMapper customerMapper;

    /**
     * Add money to a user's account
     * 
     * @param id     The ID of the user's account
     * @param amount The amount of money to add
     * @return Updated customer information as DTO
     */
    // this make no sense but i am just doing it for the sake of learning
    @PutMapping("/addMoney/{id}/{amount}")
    public CustomerDTO addMoney(@PathVariable int id, @PathVariable double amount) {
        return customerMapper.toDto(customerService.addMoney(amount, id));
    }

    /**
     * Delete a user account
     * 
     * @param id The ID of the user to delete
     */
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable int id) {
        // check if authenticated user is trying to delete their own account or if they
        // are an admin
        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        CustomerInfo authenticatedUser = customerService.getUserByUsername(authenticatedUsername);
        if (authenticatedUser == null) {
            throw new RuntimeException("Authenticated user not found in database");
        }
        if (authenticatedUser.getId() != id && !SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new RuntimeException("You can only delete your own account unless you are an admin");
        }

        // if the authenticated user is trying to delete their own account or if they
        // are an admin, proceed with deletion

        customerService.deleteUser(id);
    }

    /**
     * Transfer money between two user accounts
     * 
     * @param transferDTO DTO containing sender, receiver and amount information
     * @return Updated sender's account information as DTO
     */
    @PostMapping("/moneyTransfer")
    public CustomerDTO transferMoney(@RequestBody TransferDTO transferDTO) {

        String senderUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        CustomerInfo senderInfo = customerService.getUserByUsername(senderUsername);
        if (senderInfo == null) {
            throw new RuntimeException("Authenticated user not found in database");
        }
        if (senderInfo.getId() != transferDTO.getSenderId()) {
            throw new RuntimeException("Authenticated user does not match sender ID");
        }

        return customerMapper.toDto(customerService.transferMoney(
                transferDTO.getAmount(),
                transferDTO.getReceiverId(),
                transferDTO.getSenderId()));
    }

    /**
     * Retrieve user account details
     * 
     * @param id The ID of the user
     * @return User's account information as DTO
     */
    @GetMapping("/getMyDetail/{id}")
    public CustomerDTO getMyDetails(@PathVariable int id) {

        return customerMapper.toDto(customerService.getMyDetails(id));
    }

    /**
     * Get CSRF token for form submission security
     * 
     * @param request The HTTP request
     * @return CSRF token for the current session
     */
    @GetMapping("/get_csrf")
    public CsrfToken cf(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute("_csrf");
    }

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome to the User Panel";
    }

    /**
     * Get current user details
     * 
     * @return Current user's account information as DTO
     */
    @GetMapping("/current")
    public CustomerDTO getCurrentUser() {
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getPrincipal();
            String username = userDetails.getUsername();
            System.out.println("Getting current user info for username: " + username);

            CustomerInfo currentUserInfo = customerService.getUserByUsername(username);
            if (currentUserInfo == null) {
                System.err.println("User not found in database for username: " + username);
                throw new RuntimeException("User not found in database for username: " + username);
            }

            CustomerDTO currentUser = customerMapper.toDto(currentUserInfo);
            System.out.println("Successfully retrieved user: " + currentUser.getCustomerName());
            return currentUser;
        } catch (Exception e) {
            System.err.println("Error in getCurrentUser: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get current user: " + e.getMessage());
        }
    }

}

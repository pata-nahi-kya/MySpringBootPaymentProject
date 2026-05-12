package com.paymentproject.payment.ControllerLayer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.paymentproject.payment.ServiceStructureImplementation.ssImplementation;
import com.paymentproject.payment.dto.CustomerDTO;
import com.paymentproject.payment.dto.CustomerMapper;

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
    ssImplementation ss;

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
    @PutMapping("/addMoney/{id}/{amount}")
    public CustomerDTO addMoney(@PathVariable int id, @PathVariable int amount) {
        return customerMapper.toDto(ss.addMoney(amount, id));
    }

    /**
     * Delete a user account
     * 
     * @param id The ID of the user to delete
     */
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable int id) {
        ss.deleteUser(id);
    }

    /**
     * Transfer money between two user accounts
     * 
     * @param idr    ID of the receiver's account
     * @param ids    ID of the sender's account
     * @param amount Amount of money to transfer
     * @return Updated sender's account information as DTO
     */
    @PutMapping("/moneyTransfer/{idr}/{ids}/{amount}")
    public CustomerDTO transferMoney(@PathVariable int idr, @PathVariable int ids, @PathVariable int amount) {
        return customerMapper.toDto(ss.transferMoney(amount, idr, ids));
    }

    /**
     * Retrieve user account details
     * 
     * @param id The ID of the user
     * @return User's account information as DTO
     */
    @GetMapping("/getMyDetail/{id}")
    public CustomerDTO getMethodName(@PathVariable int id) {
        return customerMapper.toDto(ss.getMydetail(id));
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
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        return customerMapper.toDto(ss.getUserByUsername(username));
    }

}

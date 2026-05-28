package com.paymentproject.payment.ServiceStructure;

import java.util.List;

import com.paymentproject.payment.Model.CustomerInfo;
import com.paymentproject.payment.dto.CustomerDTO; // Imported the DTO
import com.paymentproject.payment.dto.OtpVerificationDTO;

/**
 * Service Structure Interface
 * 
 * This interface defines the core business operations for the banking system:
 * - User management (create, delete, retrieve)
 * - Money operations (transfer, add)
 * - Information retrieval
 * 
 * All methods should be implemented with appropriate:
 * - Security checks
 * - Transaction management
 * - Error handling
 */
public interface ServiceStructure {
    /**
     * Create a new user in the system
     * 
     * @param customer Customer information for the new user
     * @return Created customer entity with generated ID
     */
    void initiateRegister(CustomerInfo customerInfo);

    /**
     * Transfer money between two customer accounts
     * 
     * @param amount     Amount to transfer
     * @param receiverId Receiver's account ID
     * @param senderId   Sender's account ID
     * @return Updated sender's account information
     */
    CustomerInfo transferMoney(double amount, int receiverId, int senderId);

    /**
     * Add money to a customer's account
     * 
     * @param amount Amount to add
     * @param id     Customer's account ID
     * @return Updated customer information
     */
    CustomerInfo addMoney(double amount, int id);

    /**
     * Delete a user from the system
     * 
     * @param id ID of the user to delete
     */
    void deleteUser(int id);

    /**
     * Retrieve information about all users
     * 
     * @return List of all customer information
     */
    List<CustomerInfo> getAllInfo();

    /**
     * FIXED: Return type changed to CustomerDTO to support safe caching in Redis
     * Get detailed information about a specific customer
     * 
     * @param id Customer's ID
     * @return Customer's detailed information DTO
     */
    CustomerDTO getMyDetails(int id);

    /**
     * FIXED: Return type changed to CustomerDTO to prevent
     * LazyInitializationExceptions
     * Get customer information by username
     * 
     * @param username Customer's username
     * @return Customer's information DTO
     */
    CustomerDTO getUserByUsername(String username);

    /**
     * Bulk add multiple customers to the system (admin only)
     * 
     * @param customers List of customer entities to create
     * @return List of created customer entities with generated IDs
     */
    List<CustomerInfo> bulkAddCustomers(List<CustomerInfo> customers);

    boolean verifyAndRegister(OtpVerificationDTO verificationDTO);
}

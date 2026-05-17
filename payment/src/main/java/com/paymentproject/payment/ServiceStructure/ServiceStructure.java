package com.paymentproject.payment.ServiceStructure;

import java.util.List;



import com.paymentproject.payment.Model.customerInfo;

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
     * @param cs Customer information for the new user
     * @return Created customer entity with generated ID
     */
    customerInfo createUser(customerInfo cs);

    /**
     * Transfer money between two customer accounts
     * 
     * @param amount Amount to transfer
     * @param ri     Receiver's account ID
     * @param si     Sender's account ID
     * @return Updated sender's account information
     */
    customerInfo transferMoney(double amount, int ri, int si);

    /**
     * Add money to a customer's account
     * 
     * @param amount Amount to add
     * @param id     Customer's account ID
     * @return Updated customer information
     */
    customerInfo addMoney(double amount, int id);

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
    List<customerInfo> getAllInfo();

    /**
     * Get detailed information about a specific customer
     * 
     * @param id Customer's ID
     * @return Customer's detailed information
     */
    customerInfo getMydetail(int id);

    /**
     * Get customer information by username
     * 
     * @param username Customer's username
     * @return Customer's information
     */
    customerInfo getUserByUsername(String username);

    /**
     * Bulk add multiple customers to the system (admin only)
     * 
     * @param customers List of customer entities to create
     * @return List of created customer entities with generated IDs
     */
    List<customerInfo> bulkAddCustomers(List<customerInfo> customers);

    void saveToRedis(String key, Object value);
    Object getFromRedis(String key);
    boolean existsInRedis(String key);
}

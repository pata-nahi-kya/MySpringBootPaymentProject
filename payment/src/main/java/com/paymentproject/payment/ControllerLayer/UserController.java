package com.paymentproject.payment.ControllerLayer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.paymentproject.payment.ServiceStructureImplementation.CustomerServiceImpl;
import com.paymentproject.payment.dto.CustomerDTO;
import com.paymentproject.payment.dto.CustomerMapper;
import com.paymentproject.payment.dto.TransferDTO;

/**
 * User Controller
 *
 * Endpoints for user self-service operations: view own account, transfer money,
 * delete own account.
 */
@RestController
@RequestMapping("/bank/user")
public class UserController {

    @Autowired
    private CustomerServiceImpl customerService;

    @Autowired
    private CustomerMapper customerMapper;

    /**
     * Welcome endpoint for user validation verification.
     * 
     * @return 200 OK with welcome message string wrapper
     */
    @GetMapping("/welcome")
    public ResponseEntity<String> welcome() {
        return ResponseEntity.ok("Welcome to the User Panel");
    }

    /**
     * Add money to a user's account.
     *
     * @param id     account ID
     * @param amount amount to credit
     * @return 200 OK with updated account DTO payload
     */
    @PutMapping("/addMoney/{id}/{amount}")
    public ResponseEntity<CustomerDTO> addMoney(@PathVariable int id, @PathVariable double amount) {
        CustomerDTO updatedCustomer = customerMapper.toDto(customerService.addMoney(amount, id));
        return ResponseEntity.ok(updatedCustomer);
    }

    /**
     * Delete an account. A user may only delete their own account; an admin may
     * delete any account.
     *
     * @param id the account ID to delete
     * @return 204 No Content if successful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable int id) {
        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        CustomerDTO authenticatedUser = customerService.getUserByUsername(authenticatedUsername);

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (authenticatedUser.getId() != id && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You may only delete your own account");
        }

        customerService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Transfer money to another account.
     *
     * @param transferDTO transfer details (senderId, receiverId, amount)
     * @return 200 OK with updated sender account DTO payload
     */
    @PostMapping("/moneyTransfer")
    public ResponseEntity<CustomerDTO> transferMoney(@RequestBody TransferDTO transferDTO) {
        String senderUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        CustomerDTO senderInfo = customerService.getUserByUsername(senderUsername);

        if (senderInfo.getId() != transferDTO.getSenderId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Authenticated user does not match sender ID");
        }

        CustomerDTO transferResult = customerMapper.toDto(customerService.transferMoney(
                transferDTO.getAmount(),
                transferDTO.getReceiverId(),
                transferDTO.getSenderId()));
                
        return ResponseEntity.ok(transferResult);
    }

    /**
     * Retrieve account details for a specific account ID.
     *
     * @param id account ID
     * @return 200 OK with account DTO payload
     */
    @GetMapping("/getMyDetail/{id}")
    public ResponseEntity<CustomerDTO> getMyDetails(@PathVariable int id) {
        CustomerDTO customerDetails = customerService.getMyDetails(id);
        return ResponseEntity.ok(customerDetails);
    }

    /**
     * Return the currently authenticated user's account details without requiring
     * an ID in the path. Useful for the frontend "load my profile" call.
     *
     * @return 200 OK with current authenticated user's account DTO payload
     */
    @GetMapping("/current")
    public ResponseEntity<CustomerDTO> getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("Authenticated username: " + userDetails.getUsername());

        CustomerDTO currentUserInfo = customerService.getUserByUsername(userDetails.getUsername());
        return ResponseEntity.ok(currentUserInfo);
    }
}
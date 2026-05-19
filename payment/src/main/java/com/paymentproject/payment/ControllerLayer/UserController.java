package com.paymentproject.payment.ControllerLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.paymentproject.payment.Model.CustomerInfo;
import com.paymentproject.payment.ServiceStructureImplementation.CustomerServiceImpl;
import com.paymentproject.payment.dto.CustomerDTO;
import com.paymentproject.payment.dto.CustomerMapper;
import com.paymentproject.payment.dto.TransferDTO;

/**
 * User Controller
 *
 * Endpoints for user self-service operations: view own account, transfer money,
 * delete own account.
 *
 * --- Bug fixed: RuntimeException thrown directly ---
 * Throwing RuntimeException from a controller gives the client a 500 Internal
 * Server Error regardless of the actual problem (e.g. "not your account" should
 * be 403 Forbidden, "user not found" should be 404 Not Found). All throws are
 * replaced with ResponseStatusException with the correct HTTP status.
 *
 * --- Bug fixed: System.out.println / e.printStackTrace() removed ---
 * These write unstructured text to stdout with no log level, no timestamp, and
 * no thread info. Replaced with SLF4J, which integrates with Spring Boot's
 * Logback configuration, supports log levels (INFO, WARN, ERROR), and can be
 * routed to log aggregators (ELK, Datadog, etc.).
 *
 * --- Improvement: getCurrentUser does not need a try/catch ---
 * The original method wrapped everything in a try/catch that re-threw as
 * RuntimeException, losing the original HTTP semantics. The service layer
 * already throws ResponseStatusException; there is nothing to catch here.
 */
@RestController
@RequestMapping("/bank/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private CustomerServiceImpl customerService;

    @Autowired
    private CustomerMapper customerMapper;

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome to the User Panel";
    }

    /**
     * Add money to a user's account.
     *
     * Note: in a real banking system this endpoint would not exist in this form.
     * Only authorised cashiers or payment gateways should be able to credit
     * accounts. Left here as a learning exercise per the original comments.
     *
     * @param id     account ID
     * @param amount amount to credit
     * @return updated account DTO
     */
    @PutMapping("/addMoney/{id}/{amount}")
    public CustomerDTO addMoney(@PathVariable int id, @PathVariable double amount) {
        return customerMapper.toDto(customerService.addMoney(amount, id));
    }

    /**
     * Delete an account. A user may only delete their own account; an admin may
     * delete any account.
     *
     * Bug fixed: RuntimeException replaced with ResponseStatusException so the
     * correct HTTP status (403 Forbidden) is returned to the client.
     *
     * @param id the account ID to delete
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable int id) {
        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        CustomerInfo authenticatedUser = customerService.getUserByUsername(authenticatedUsername);

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
     * The sender ID in the request body is validated against the authenticated
     * user to prevent one user from initiating a transfer on behalf of another.
     *
     * @param transferDTO transfer details (senderId, receiverId, amount)
     * @return updated sender account DTO
     */
    @PostMapping("/moneyTransfer")
    public CustomerDTO transferMoney(@RequestBody TransferDTO transferDTO) {
        String senderUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        CustomerInfo senderInfo = customerService.getUserByUsername(senderUsername);

        if (senderInfo.getId() != transferDTO.getSenderId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Authenticated user does not match sender ID");
        }

        return customerMapper.toDto(customerService.transferMoney(
                transferDTO.getAmount(),
                transferDTO.getReceiverId(),
                transferDTO.getSenderId()));
    }

    /**
     * Retrieve account details for a specific account ID.
     * The service layer enforces that the caller can only see their own account.
     *
     * @param id account ID
     * @return account DTO
     */
    @GetMapping("/getMyDetail/{id}")
    public CustomerDTO getMyDetails(@PathVariable int id) {
        return customerMapper.toDto(customerService.getMyDetails(id));
    }

    /**
     * Return the currently authenticated user's account details without requiring
     * an ID in the path. Useful for the frontend "load my profile" call.
     *
     * Bug fixed: removed try/catch that swallowed HTTP-aware exceptions and turned
     * them into generic 500s. The service layer throws ResponseStatusException with
     * correct status codes; Spring MVC handles those automatically.
     *
     * @return current user's account DTO
     */
    @GetMapping("/current")
    public CustomerDTO getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        log.debug("Fetching current user info for username={}", userDetails.getUsername());
        CustomerInfo currentUserInfo = customerService.getUserByUsername(userDetails.getUsername());
        return customerMapper.toDto(currentUserInfo);
    }
}

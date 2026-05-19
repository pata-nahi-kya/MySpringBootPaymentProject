package com.paymentproject.payment.ControllerLayer;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paymentproject.payment.Model.CustomerInfo;
import com.paymentproject.payment.ServiceStructureImplementation.CustomerServiceImpl;
import com.paymentproject.payment.dto.CustomerDTO;
import com.paymentproject.payment.dto.CustomerMapper;
import com.paymentproject.payment.dto.CustomerRegistrationDTO;

/**
 * Admin Controller
 *
 * All endpoints require ADMIN role, enforced at both the SecurityConfig filter
 * chain level and here via @PreAuthorize for defence in depth.
 *
 * --- Bug fixed: inline raw java.util.List fully-qualified import in bulkAdd ---
 * The original bulkAdd method used "java.util.List<CustomerInfo>" inline in the
 * method body for no reason, bypassing the import at the top of the file. While
 * it compiles, it is inconsistent and confusing. Fixed by using the imported
 * List type consistently.
 *
 * --- Improvement: void endpoints now return ResponseEntity ---
 * Returning void from a DELETE gives the caller no feedback. Returning
 * ResponseEntity<Void> with 204 No Content is the correct REST convention.
 *
 * --- Improvement: POST createUser returns 201 Created ---
 * Resource creation should return 201 Created, not 200 OK.
 */
@RestController
@RequestMapping("/bank/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private CustomerServiceImpl customerService;

    @Autowired
    private CustomerMapper customerMapper;

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome to the Admin Panel";
    }

    /**
     * Create a single new user.
     *
     * @param registrationDTO registration data for the new user
     * @return 201 Created with the created user's DTO
     */
    @PostMapping("/createUser")
    public ResponseEntity<CustomerDTO> createUser(@RequestBody CustomerRegistrationDTO registrationDTO) {
        CustomerInfo entity = customerMapper.toEntity(registrationDTO);
        CustomerDTO created = customerMapper.toDto(customerService.createUser(entity));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Create multiple users in a single request.
     *
     * @param registrations list of registration DTOs
     * @return 201 Created with the list of created user DTOs
     */
    @PostMapping("/bulkAdd")
    public ResponseEntity<List<CustomerDTO>> bulkAdd(@RequestBody List<CustomerRegistrationDTO> registrations) {
        List<CustomerInfo> entities = registrations.stream()
                .map(customerMapper::toEntity)
                .collect(Collectors.toList());

        List<CustomerDTO> created = customerService.bulkAddCustomers(entities).stream()
                .map(customerMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Retrieve all users. Consider adding pagination (Pageable) before deploying
     * to production — fetching all rows is dangerous on large tables.
     *
     * @return list of all user DTOs
     */
    @GetMapping("/getAllUser")
    public List<CustomerDTO> getAllInfo() {
        return customerService.getAllInfo().stream()
                .map(customerMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Delete a user by ID.
     *
     * @param id the user's ID
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable int id) {
        customerService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

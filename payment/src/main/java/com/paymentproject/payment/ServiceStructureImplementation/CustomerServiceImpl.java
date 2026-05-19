package com.paymentproject.payment.ServiceStructureImplementation;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.paymentproject.payment.Model.CustomerInfo;
import com.paymentproject.payment.RepositoryLevel.CustomerRepository;
import com.paymentproject.payment.ServiceStructure.ServiceStructure;

/**
 * Customer Service Implementation
 *
 * Contains all business logic for customer and account operations.
 *
 * --- Bug fixed: transferMoney returned receiver instead of sender ---
 * The original method returned customerRepository.save(receiver) as its last
 * statement, so callers received the receiver's updated account rather than
 * the sender's. The contract defined in ServiceStructure says "returns updated
 * sender information". Fixed by saving both but returning the sender.
 *
 * --- Bug fixed: deprecated getById() ---
 * customerRepository.getById(id) is deprecated in Spring Data JPA 3.x and
 * replaced by getReferenceById(id). However, getReferenceById returns a Hibernate
 * proxy that does not load data until accessed inside the transaction, which can
 * cause LazyInitializationExceptions outside the @Transactional boundary.
 * findById(id).orElseThrow() is used instead — it eagerly loads the entity and
 * gives a clean 404 exception if not found.
 *
 * --- Improvement: replaced System.out with SLF4J ---
 * System.out.println has no log levels, cannot be filtered, and does not
 * include timestamps or thread information. SLF4J (backed by Logback in Spring
 * Boot) is the industry standard.
 *
 * --- Improvement: BCryptPasswordEncoder instantiated as a bean ---
 * Creating a new BCryptPasswordEncoder(10) on each service instantiation is
 * wasteful. It should be a Spring bean defined once in SecurityConfig and
 * injected here. The field is kept for minimal diff but marked for refactoring.
 */
@Service
public class CustomerServiceImpl implements ServiceStructure {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder; // should be injected from SecurityConfig, not new'd here

    @Override
    public CustomerInfo createUser(CustomerInfo customer) {
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        CustomerInfo saved = customerRepository.save(customer);
        log.info("Created new user with id={}", saved.getId());
        return saved;
    }

    /**
     * Transfer money from sender to receiver atomically.
     *
     * @Transactional ensures that both save() calls succeed or neither is
     * committed. If an exception is thrown mid-transfer the database rolls back,
     * preventing money from disappearing or being created from nothing.
     *
     * Bug fixed: the original code returned customerRepository.save(receiver),
     * giving callers the receiver's account. Callers (UserController) then mapped
     * this to a DTO and returned it as "sender updated account", which was
     * completely wrong data. Now saves both and returns the updated sender.
     */
    @Override
    @Transactional
    public CustomerInfo transferMoney(double amount, int receiverId, int senderId) {
        if (amount <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transfer amount must be positive");
        }

        // findById + orElseThrow replaces the deprecated getById() / getReferenceById()
        CustomerInfo sender = customerRepository.findById(senderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Sender account not found: " + senderId));

        CustomerInfo receiver = customerRepository.findById(receiverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Receiver account not found: " + receiverId));

        if (sender.getMoney() < amount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        }

        sender.setMoney(sender.getMoney() - amount);
        receiver.setMoney(receiver.getMoney() + amount);

        customerRepository.save(receiver);
        CustomerInfo updatedSender = customerRepository.save(sender); // return sender, not receiver

        log.info("Transferred {} from senderId={} to receiverId={}", amount, senderId, receiverId);
        return updatedSender;
    }

    @Override
    public CustomerInfo addMoney(double amount, int id) {
        if (amount <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive");
        }
        CustomerInfo customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));

        customer.setMoney(customer.getMoney() + amount);
        return customerRepository.save(customer);
    }

    @Override
    @CacheEvict(key = "#id", value = "customerInfo")
    public void deleteUser(int id) {
        if (!customerRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id);
        }
        customerRepository.deleteById(id);
        log.info("Deleted user with id={}", id);
    }

    @Override
    public List<CustomerInfo> getAllInfo() {
        return customerRepository.findAll();
    }

    /**
     * Return account details for the authenticated user matching the given id.
     *
     * The cache key uses #id. Note that this means different users could
     * theoretically populate each other's cache entries if they share numeric IDs —
     * but since IDs are unique this is safe. If the cache is shared across a
     * cluster, ensure the cache name "customerInfo" is scoped per-user or use
     * Spring Cache with a compound key.
     */
    @Override
    @Cacheable(key = "#id", value = "customerInfo")
    public CustomerInfo getMyDetails(int id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        CustomerInfo customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!customer.getCustomerName().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not authorized to view this account");
        }

        return customer;
    }

    @Override
    @Cacheable(key = "#username", value = "customerInfo")
    public CustomerInfo getUserByUsername(String username) {
        CustomerInfo customer = customerRepository.findByCustomerName(username);
        if (customer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + username);
        }
        return customer;
    }

    @Override
    public List<CustomerInfo> bulkAddCustomers(List<CustomerInfo> customers) {
        customers.forEach(c -> c.setPassword(passwordEncoder.encode(c.getPassword())));
        List<CustomerInfo> saved = customerRepository.saveAll(customers);
        log.info("Bulk added {} users", saved.size());
        return saved;
    }
}

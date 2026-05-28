package com.paymentproject.payment.ServiceStructureImplementation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentproject.payment.Model.CustomerInfo;
import com.paymentproject.payment.Model.Role;
import com.paymentproject.payment.RepositoryLevel.CustomerRepository;
import com.paymentproject.payment.ServiceStructure.ServiceStructure;
import com.paymentproject.payment.config.SecurityConfig;
import com.paymentproject.payment.dto.CustomerDTO;
import com.paymentproject.payment.dto.CustomerMapper;
import com.paymentproject.payment.dto.CustomerRegistrationDTO;
import com.paymentproject.payment.dto.OtpVerificationDTO;

@Service
public class CustomerServiceImpl implements ServiceStructure {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private EmailServiceImpl emailServiceImpl;

    @Autowired
    private SecurityConfig sc;

    // @Override
    // public CustomerInfo createUser(CustomerInfo customer) {
    // if (customerRepository.existsByEmail(customer.getEmail())) {
    // throw new RuntimeException("Email configuration is already registered to a
    // system profile.");
    // }
    // if (customerRepository.existsByCustomerName(customer.getCustomerName())) {
    // throw new RuntimeException("Username is already taken by another account
    // profile.");
    // }

    // customer.setPassword(passwordEncoder.encode(customer.getPassword()));
    // CustomerInfo saved = customerRepository.save(customer);
    // log.info("Created new user with id={}", saved.getId());
    // return saved;
    // }

    /**
     * FIXED: Changed cache clearing strategy to avoid complex 'result' field
     * evaluations.
     * This explicitly evicts entries by their numeric IDs using clean parameters.
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(key = "#senderId", value = "customerInfo"),
            @CacheEvict(key = "#receiverId", value = "customerInfo"),
            @CacheEvict(value = "customerInfo", allEntries = true) // Flushes all string username keys globally to stay
                                                                   // safe
    })
    public CustomerInfo transferMoney(double amount, int receiverId, int senderId) {
        if (amount <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transfer amount must be positive");
        }

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
        CustomerInfo updatedSender = customerRepository.save(sender);

        log.info("Transferred {} from senderId={} to receiverId={}", amount, senderId, receiverId);
        return updatedSender;
    }

    /**
     * FIXED: Cleaned eviction matching to use plain, compilation-safe parameters.
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(key = "#id", value = "customerInfo"),
            @CacheEvict(value = "customerInfo", allEntries = true)
    })
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
    @Transactional
    @CacheEvict(value = "customerInfo", allEntries = true, beforeInvocation = true)
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

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#id", value = "customerInfo")
    public CustomerDTO getMyDetails(int id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        CustomerInfo customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!customer.getCustomerName().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not authorized to view this account");
        }

        return customerMapper.toDto(customer);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#username", value = "customerInfo")
    public CustomerDTO getUserByUsername(String username) {
        CustomerInfo customer = customerRepository.findByCustomerName(username);
        if (customer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + username);
        }
        return customerMapper.toDto(customer);
    }

    @Override
    @Transactional
    @CacheEvict(value = "customerInfo", allEntries = true)
    public List<CustomerInfo> bulkAddCustomers(List<CustomerInfo> customers) {
        customers.forEach(c -> c.setPassword(passwordEncoder.encode(c.getPassword())));
        List<CustomerInfo> saved = customerRepository.saveAll(customers);
        log.info("Bulk added {} users", saved.size());
        return saved;
    }

    private static final String REDIS_PREFIX = "PENDING_REG:";
    private static final long OTP_EXPIRY_MINUTES = 5;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void initiateRegister(CustomerInfo customerInfo) {

        String otp = String.format("%06d", new Random().nextInt(999999));
        System.out.println("generated otp " + otp);
        Map<String, Object> registrationData = new HashMap<>();
        registrationData.put("userData", customerInfo);
        registrationData.put("otp", otp);
        String email = customerInfo.getEmail();

        String redisKey = REDIS_PREFIX + email;
        System.out.println("stored value in redis");
        redisTemplate.opsForValue().set(redisKey, registrationData, OTP_EXPIRY_MINUTES, TimeUnit.MINUTES);
        String subject = "Your otp for verification";
        String body = "otp for your verification is " + otp;

        System.out.println("sending email");
        emailServiceImpl.sendSimpleEmail(email, subject, body);
    }

    @Override
    public boolean verifyAndRegister(OtpVerificationDTO verificationDTO) {
        String redisKey = REDIS_PREFIX + verificationDTO.getEmail();
        // 1. Retrieve raw object from Redis safely
        Object rawData = redisTemplate.opsForValue().get(redisKey);

        if (rawData == null) {
            throw new RuntimeException("OTP has expired or registration request does not exist.");
        }

        // 2. Safely map it using ObjectMapper
        ObjectMapper mapper = new ObjectMapper();
        Map<?, ?> cachedData = mapper.convertValue(rawData, Map.class);

        // 3. Extract your specific data points safely
        String actualOtp = String.valueOf(cachedData.get("otp"));
        CustomerRegistrationDTO registrationDTO = mapper.convertValue(cachedData.get("userData"),
                CustomerRegistrationDTO.class);

        if (!actualOtp.equals(verificationDTO.getOtp())) {
            return false; // Invalid OTP entered
        }

        CustomerInfo customer = new CustomerInfo();
        customer.setCustomerName(registrationDTO.getCustomerName());
        customer.setEmail(registrationDTO.getEmail());
        customer.setPassword(sc.passwordEncoder().encode(registrationDTO.getPassword())); // Remember to encode password
                                                                                          // before saving!

        if (registrationDTO.getRole() != null && !registrationDTO.getRole().isEmpty()) {
            // Simply copy or convert your collection into a Set of Roles
            Set<Role> assignedRoles = new HashSet<>(registrationDTO.getRole());

            customer.setRole(assignedRoles);
        }

        customer.setMoney(registrationDTO.getMoney());

        customerRepository.save(customer);
        redisTemplate.delete(redisKey); // Clear cache cleanly after success
        return true;
    }

}

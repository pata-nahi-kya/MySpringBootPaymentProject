package com.paymentproject.payment.userDetailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.paymentproject.payment.Model.CustomerInfo;
import com.paymentproject.payment.RepositoryLevel.CustomerRepository;

/**
 * Custom UserDetailsService Implementation
 *
 * --- Bug fixed: @Configuration instead of @Service ---
 * The original class was annotated with @Configuration. While Spring will still
 * create it as a bean, @Configuration has a specific meaning: it tells Spring
 * to apply CGLIB subclassing so that @Bean method calls within the class are
 * intercepted. Using it on a service class:
 *   1. Is semantically incorrect and misleading to anyone reading the code.
 *   2. Causes CGLIB to proxy the class unnecessarily, adding overhead.
 *   3. Can cause unexpected behaviour if the class is later extended or if
 *      @Bean methods are added by mistake.
 *
 * The correct annotation for a service implementing a Spring interface is
 * @Service.
 */
@Service
public class CustomUserDetailService implements UserDetailsService {

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Load a user record by username for Spring Security's authentication flow.
     *
     * Spring Security calls this method when processing a login attempt. The
     * returned UserDetails object is compared against the submitted credentials.
     *
     * @param username the username supplied at login
     * @return UserDetails wrapping the CustomerInfo entity
     * @throws UsernameNotFoundException if no user exists with that username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CustomerInfo customer = customerRepository.findByCustomerName(username);
        if (customer == null) {
            // Do not reveal whether the username exists; a generic message is safer,
            // but Spring Security already wraps this in a BadCredentialsException
            // before it reaches the client, so the exact message here is fine.
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return new UserCurrent(customer);
    }
}

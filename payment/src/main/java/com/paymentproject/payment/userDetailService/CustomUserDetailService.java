package com.paymentproject.payment.userDetailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.paymentproject.payment.Model.CustomerInfo;
import com.paymentproject.payment.RepositoryLevel.CustomerRepository;

@Configuration
public class CustomUserDetailService implements UserDetailsService {

    @Autowired
    CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CustomerInfo customer = customerRepository.findByCustomerName(username);
        if (customer == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return new UserCurrent(customer);
    }

}

package com.paymentproject.payment.userDetailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.paymentproject.payment.Model.customerInfo;
import com.paymentproject.payment.RepositoryLevel.repository;

@Configuration
public class CustomUserDetailService implements UserDetailsService {

    @Autowired
    repository rp;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        customerInfo ci = rp.findByCustomerName(username);
        if (ci == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return new UserCurrent(ci);
    }

}

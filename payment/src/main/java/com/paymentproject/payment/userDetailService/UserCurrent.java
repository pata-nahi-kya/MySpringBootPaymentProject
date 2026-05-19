package com.paymentproject.payment.userDetailService;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.paymentproject.payment.Model.CustomerInfo;

public class UserCurrent implements UserDetails {
    CustomerInfo ci;

    public UserCurrent(CustomerInfo ci) {
        this.ci = ci;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return ci.getRole().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.name())).toList();
    }

    @Override
    public String getPassword() {
        return ci.getPassword();
    }

    @Override
    public String getUsername() {
        return ci.getCustomerName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

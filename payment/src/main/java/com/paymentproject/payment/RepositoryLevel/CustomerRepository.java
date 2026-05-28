package com.paymentproject.payment.RepositoryLevel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.paymentproject.payment.Model.CustomerInfo;
/**
 * Customer Repository Interface
 * 
 * Extends JpaRepository to provide CRUD operations for CustomerInfo entities.
 * Custom query methods follow Spring Data naming conventions.
 */@Repository
public interface CustomerRepository extends JpaRepository<CustomerInfo, Integer> {
    // Custom query method to find customer by name , we just have to write the
    // method name in a specific format and spring will automatically create the
    // query for us ex. findBy<fieldname>
    CustomerInfo findByCustomerName(String username);

    boolean existsByEmail(String email);

    boolean existsByCustomerName(String customerName);

}

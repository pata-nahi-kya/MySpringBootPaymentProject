package com.paymentproject.payment.RepositoryLevel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.paymentproject.payment.Model.customerInfo;

@Repository
public interface repository extends JpaRepository<customerInfo, Integer> {
    // Custom query method to find customer by name , we just have to write the
    // method name in a specific format and spring will automatically create the
    // query for us ex. findBy<fieldname>
    customerInfo findByCustomerName(String username);

}

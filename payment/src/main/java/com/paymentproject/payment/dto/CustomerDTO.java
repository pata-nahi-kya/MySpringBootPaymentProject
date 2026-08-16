package com.paymentproject.payment.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.Set;



@Data
public class CustomerDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String customerName;
    private String email;

    private double money;
    private Set<String> role; // Ensure Role is a simple java enum, not a JPA Entity!
}

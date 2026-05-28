package com.paymentproject.payment.Model;

import java.io.Serializable;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "rakshit", schema = "paymentschema")
@Data
public class CustomerInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Changed from 'int' to 'Integer' to prevent default 0 assignments

    @Column(name = "CustomerName")
    private String customerName;

    @Column
    private double money;

    @Column(unique = true) // Recommended to prevent duplicate registrations
    private String email;

    @Column
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "user_roles", 
        schema = "paymentschema", // Added schema consistency anchor
        joinColumns = @JoinColumn(name = "customer_id", referencedColumnName = "id") // Explicit reference
    )
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Set<Role> role;
}

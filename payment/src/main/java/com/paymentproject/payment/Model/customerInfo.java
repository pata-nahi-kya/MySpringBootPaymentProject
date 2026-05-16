package com.paymentproject.payment.Model;

import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Customer Entity Class
 * 
 * This entity represents a customer in the banking system and stores:
 * - Personal information (ID, name)
 * - Account details (money balance)
 * - Security information (password, roles)
 * 
 * The entity uses:
 * - JPA annotations for ORM mapping
 * - Lombok @Data for automatic getter/setter generation
 * - Custom money manipulation methods
 * 
 * Database Configuration:
 * - Table name: rakshit
 * - Schema: paymentschema
 */
@Entity
@Table(name = "rakshit", schema = "paymentschema")
@Data
public class customerInfo {
    /**
     * Unique identifier for the customer
     * Auto-generated using identity strategy
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    /**
     * Customer's username/name in the system
     */
    @Column
    String customerName;

    /**
     * Customer's account balance
     */
    @Column
    double money;

    /**
     * Customer's hashed password
     * Note: This field should never be exposed in DTOs
     */
    @Column
    String password;

    /**
     * Set of roles assigned to the customer
     * Configured with:
     * - Eager loading for immediate role access
     * - Separate table for role storage
     * - String-based enum storage
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "customer_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    Set<Role> role;

    /**
     * Increases the customer's account balance
     * 
     * @param amount The amount to add to the balance
     */
    void increaseMoney(int amount) {
        money = money + amount;
    }

}

package com.paymentproject.payment.Model;

import java.io.Serializable;
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
 * Customer Entity
 *
 * Maps to the "rakshit" table in the "paymentschema" schema.
 *
 * --- Bug fixed: CustomerName field naming ---
 * Java field naming convention requires camelCase starting with a lowercase
 * letter: "customerName", not "CustomerName". Lombok @Data generates
 * getters/setters based on the field name, so "CustomerName" generates
 * "getCustomerName()" which looks correct but violates the JavaBeans spec
 * (which expects "getCustomerName" from field "customerName"). Some
 * serialization libraries (Jackson, for example) can behave inconsistently
 * with PascalCase field names. Renamed to "customerName".
 *
 * Note: this is a BREAKING CHANGE if your database column is named
 * "CustomerName". Add @Column(name = "CustomerName") to preserve the existing
 * column name while fixing the Java field name. This is done below.
 *
 * --- Bug fixed: increaseMoney visibility ---
 * The original increaseMoney method was package-private (no modifier) and
 * unused anywhere in the codebase. Money manipulation is done directly via
 * setMoney() in the service layer. The method is removed to reduce dead code.
 * If you want to restore it, make it public and call it from the service.
 *
 * --- Note on @Data ---
 * Lombok @Data generates equals() and hashCode() based on ALL fields including
 * mutable ones (money, password). This can cause problems with JPA (entities
 * in a Set may change hash code after being persisted). Consider replacing
 * @Data with @Getter @Setter @ToString and manually implementing equals/hashCode
 * based on the @Id field only for a production-grade entity.
 */
@Entity
@Table(name = "rakshit", schema = "paymentschema")
@Data
public class CustomerInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /**
     * The @Column(name = ...) preserves backward compatibility with the existing
     * database column name while fixing the Java field naming convention.
     */
    @Column(name = "CustomerName")
    private String customerName;

    @Column
    private double money;

    /**
     * Hashed password. Never include this field in any DTO or API response.
     */
    @Column
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "customer_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Set<Role> role;
}

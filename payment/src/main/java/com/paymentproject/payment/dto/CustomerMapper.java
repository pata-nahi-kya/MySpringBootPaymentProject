package com.paymentproject.payment.dto;

import org.springframework.stereotype.Component;

import com.paymentproject.payment.Model.customerInfo;

/**
 * Customer Entity-DTO Mapper
 * 
 * This component handles the conversion between:
 * - Customer entities and DTOs
 * - Registration DTOs and entities
 * 
 * Key responsibilities:
 * - Mapping between different object types
 * - Protecting sensitive data during conversion
 * - Maintaining data consistency
 * 
 * Uses @Component for Spring dependency injection
 */
@Component
public class CustomerMapper {

    /**
     * Convert a customer entity to its DTO representation
     * 
     * @param customer The customer entity to convert
     * @return DTO containing non-sensitive customer information
     */
    public CustomerDTO toDto(customerInfo customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setCustomerName(customer.getCustomerName());
        dto.setMoney(customer.getMoney());
        dto.setRole(customer.getRole());
        return dto;
    }

    /**
     * Convert a registration DTO to a customer entity
     * 
     * @param dto The registration DTO to convert
     * @return New customer entity with initial values set
     */
    public customerInfo toEntity(CustomerRegistrationDTO dto) {
        customerInfo customer = new customerInfo();
        customer.setCustomerName(dto.getCustomerName());
        customer.setPassword(dto.getPassword());
        customer.setRole(dto.getRole());
        double money = dto.getMoney() > 0 ? dto.getMoney() : 0; // Ensure money is non-negative
        customer.setMoney(money); // Set initial money to the validated value
        return customer;
    }
}
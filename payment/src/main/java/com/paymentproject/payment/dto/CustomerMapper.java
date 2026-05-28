package com.paymentproject.payment.dto;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.paymentproject.payment.Model.CustomerInfo;
import com.paymentproject.payment.Model.Role;

/**
 * Customer Entity-DTO Mapper
 * 
 * Handles bidirectional conversion between database entities and safe DTOs.
 */
@Component
public class CustomerMapper {

    /**
     * Convert a CustomerInfo database entity into a safe CustomerDTO.
     * Maps roles explicitly into pure Strings to detach Hibernate proxy connections.
     */
    public CustomerDTO toDto(CustomerInfo customer) {
        if (customer == null) {
            return null;
        }

        CustomerDTO dto = new CustomerDTO();
        dto.setCustomerName(customer.getCustomerName()); 
        dto.setMoney(customer.getMoney());
        dto.setEmail(customer.getEmail());
        dto.setId(customer.getId());
        
        if (customer.getRole() != null) {
            Set<String> safeRoles = customer.getRole().stream()
                    .map(Enum::name)
                    .collect(Collectors.toSet());
            dto.setRole(safeRoles); 
        } else {
            dto.setRole(new HashSet<>());
        }

        return dto;
    }

    /**
     * Convert a Registration DTO into a CustomerInfo database entity.
     * Safely maps role configurations to match your database field requirements.
     */
    public CustomerInfo toEntity(CustomerRegistrationDTO dto) {
        if (dto == null) {
            return null;
        }

        CustomerInfo entity = new CustomerInfo();
        entity.setCustomerName(dto.getCustomerName());
        entity.setMoney(dto.getMoney());
        entity.setEmail(dto.getEmail());
        entity.setPassword(dto.getPassword());
        
        if (dto.getRole() != null) {
            // SAFE MAP: Handles conversion to target enum safely regardless of your DTO property type definition
            Set<Role> enumRoles = dto.getRole().stream()
                    .map(roleObj -> {
                        if (roleObj instanceof Role) {
                            return (Role) roleObj;
                        }
                        return Role.valueOf(roleObj.toString().toUpperCase().trim());
                    })
                    .collect(Collectors.toSet());
            entity.setRole(enumRoles); 
        } else {
            entity.setRole(new HashSet<>());
        }
        
        return entity;
    }
}

package com.nahid.customer.service;


import com.nahid.customer.dto.CustomerRequestDto;
import com.nahid.customer.dto.CustomerResponseDto;
import com.nahid.customer.enums.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {

    CustomerResponseDto createCustomer(CustomerRequestDto customerRequest);

    CustomerResponseDto getCustomerById(String customerId);

    CustomerResponseDto getCustomerByEmail(String email);

    CustomerResponseDto updateCustomer(String customerId, CustomerRequestDto customerRequest);

    void deleteCustomer(String customerId);

    Page<CustomerResponseDto> getAllCustomers(Pageable pageable);

    Page<CustomerResponseDto> getCustomersByStatus(CustomerStatus status, Pageable pageable);

    Page<CustomerResponseDto> searchCustomers(String searchTerm, Pageable pageable);

    CustomerResponseDto updateCustomerStatus(String customerId, CustomerStatus status);
}
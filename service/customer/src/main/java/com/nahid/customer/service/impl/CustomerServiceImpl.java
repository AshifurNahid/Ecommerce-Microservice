package com.nahid.customer.service.impl;


import com.nahid.customer.dto.CustomerRequestDto;
import com.nahid.customer.dto.CustomerResponseDto;
import com.nahid.customer.entity.Customer;
import com.nahid.customer.enums.CustomerStatus;
import com.nahid.customer.exception.CustomerAlreadyExistsException;
import com.nahid.customer.exception.CustomerNotFoundException;
import com.nahid.customer.mapper.CustomerMapper;
import com.nahid.customer.repository.CustomerRepository;
import com.nahid.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional
    public CustomerResponseDto createCustomer(CustomerRequestDto customerRequest) {
        log.info("Creating customer with email: {}", customerRequest.getEmail());

        if (customerRepository.existsByEmail(customerRequest.getEmail())) {
            log.warn("Customer with email {} already exists", customerRequest.getEmail());
            throw new CustomerAlreadyExistsException("Customer with email " + customerRequest.getEmail() + " already exists");
        }

        Customer customer = customerMapper.toEntity(customerRequest);
        Customer savedCustomer = customerRepository.save(customer);

        log.info("Customer created successfully with ID: {}", savedCustomer.getId());
        return customerMapper.toResponseDto(savedCustomer);
    }

    @Override
    public CustomerResponseDto getCustomerById(String customerId) {
        log.info("Fetching customer with ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    log.warn("Customer not found with ID: {}", customerId);
                    return new CustomerNotFoundException("Customer not found with ID: " + customerId);
                });

        return customerMapper.toResponseDto(customer);
    }

    @Override
    public CustomerResponseDto getCustomerByEmail(String email) {
        log.info("Fetching customer with email: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Customer not found with email: {}", email);
                    return new CustomerNotFoundException("Customer not found with email: " + email);
                });

        return customerMapper.toResponseDto(customer);
    }

    @Override
    @Transactional
    public CustomerResponseDto updateCustomer(String customerId, CustomerRequestDto customerRequest) {
        log.info("Updating customer with ID: {}", customerId);

        Customer existingCustomer = customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    log.warn("Customer not found with ID: {}", customerId);
                    return new CustomerNotFoundException("Customer not found with ID: " + customerId);
                });

        // Check if email is being changed and if new email already exists
        if (!existingCustomer.getEmail().equals(customerRequest.getEmail()) &&
                customerRepository.existsByEmail(customerRequest.getEmail())) {
            log.warn("Email {} already exists for another customer", customerRequest.getEmail());
            throw new CustomerAlreadyExistsException("Email " + customerRequest.getEmail() + " already exists");
        }

        customerMapper.updateEntityFromDto(customerRequest, existingCustomer);
        Customer updatedCustomer = customerRepository.save(existingCustomer);

        log.info("Customer updated successfully with ID: {}", updatedCustomer.getId());
        return customerMapper.toResponseDto(updatedCustomer);
    }

    @Override
    @Transactional
    public void deleteCustomer(String customerId) {
        log.info("Deleting customer with ID: {}", customerId);

        if (!customerRepository.existsById(customerId)) {
            log.warn("Customer not found with ID: {}", customerId);
            throw new CustomerNotFoundException("Customer not found with ID: " + customerId);
        }

        customerRepository.deleteById(customerId);
        log.info("Customer deleted successfully with ID: {}", customerId);
    }

    @Override
    public Page<CustomerResponseDto> getAllCustomers(Pageable pageable) {
        log.info("Fetching all customers with pagination");

        return customerRepository.findAll(pageable)
                .map(customerMapper::toResponseDto);
    }

    @Override
    public Page<CustomerResponseDto> getCustomersByStatus(CustomerStatus status, Pageable pageable) {
        log.info("Fetching customers with status: {}", status);

        return customerRepository.findByStatus(status, pageable)
                .map(customerMapper::toResponseDto);
    }

    @Override
    public Page<CustomerResponseDto> searchCustomers(String searchTerm, Pageable pageable) {
        log.info("Searching customers with term: {}", searchTerm);

        return customerRepository.searchCustomers(searchTerm, pageable)
                .map(customerMapper::toResponseDto);
    }

    @Override
    @Transactional
    public CustomerResponseDto updateCustomerStatus(String customerId, CustomerStatus status) {
        log.info("Updating customer status for ID: {} to {}", customerId, status);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    log.warn("Customer not found with ID: {}", customerId);
                    return new CustomerNotFoundException("Customer not found with ID: " + customerId);
                });

        customer.setStatus(status);
        Customer updatedCustomer = customerRepository.save(customer);

        log.info("Customer status updated successfully for ID: {}", customerId);
        return customerMapper.toResponseDto(updatedCustomer);
    }
}
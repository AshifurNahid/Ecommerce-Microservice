package com.nahid.customer.controller;

import com.nahid.customer.dto.CustomerRequestDto;
import com.nahid.customer.dto.CustomerResponseDto;
import com.nahid.customer.enums.CustomerStatus;
import com.nahid.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponseDto> createCustomer(@Valid @RequestBody CustomerRequestDto customerRequest) {
        log.info("Received request to create customer with email: {}", customerRequest.getEmail());
        CustomerResponseDto response = customerService.createCustomer(customerRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponseDto> getCustomerById(@PathVariable String customerId) {
        log.info("Received request to get customer with ID: {}", customerId);
        CustomerResponseDto response = customerService.getCustomerById(customerId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<CustomerResponseDto> getCustomerByEmail(@PathVariable String email) {
        log.info("Received request to get customer with email: {}", email);
        CustomerResponseDto response = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerResponseDto> updateCustomer(
            @PathVariable String customerId,
            @Valid @RequestBody CustomerRequestDto customerRequest) {
        log.info("Received request to update customer with ID: {}", customerId);
        CustomerResponseDto response = customerService.updateCustomer(customerId, customerRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable String customerId) {
        log.info("Received request to delete customer with ID: {}", customerId);
        customerService.deleteCustomer(customerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<CustomerResponseDto>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        log.info("Received request to get all customers - page: {}, size: {}", page, size);

        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CustomerResponseDto> response = customerService.getAllCustomers(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<CustomerResponseDto>> getCustomersByStatus(
            @PathVariable CustomerStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Received request to get customers with status: {}", status);
        Pageable pageable = PageRequest.of(page, size);
        Page<CustomerResponseDto> response = customerService.getCustomersByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CustomerResponseDto>> searchCustomers(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Received request to search customers with term: {}", searchTerm);
        Pageable pageable = PageRequest.of(page, size);
        Page<CustomerResponseDto> response = customerService.searchCustomers(searchTerm, pageable);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{customerId}/status")
    public ResponseEntity<CustomerResponseDto> updateCustomerStatus(
            @PathVariable String customerId,
            @RequestParam CustomerStatus status) {

        log.info("Received request to update status for customer ID: {} to {}", customerId, status);
        CustomerResponseDto response = customerService.updateCustomerStatus(customerId, status);
        return ResponseEntity.ok(response);
    }
}

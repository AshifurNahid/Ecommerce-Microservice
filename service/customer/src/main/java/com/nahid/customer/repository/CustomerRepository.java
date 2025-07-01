package com.nahid.customer.repository;

import com.nahid.customer.entity.Customer;
import com.nahid.customer.enums.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("{'status': ?0}")
    Page<Customer> findByStatus(CustomerStatus status, Pageable pageable);

    @Query("{'$or': [ " +
            "{'firstName': { $regex: ?0, $options: 'i' }}, " +
            "{'lastName': { $regex: ?0, $options: 'i' }}, " +
            "{'email': { $regex: ?0, $options: 'i' }} " +
            "]}")
    Page<Customer> searchCustomers(String searchTerm, Pageable pageable);
}
package com.m2ibank.customer.service;

import com.m2ibank.common.exception.BusinessException;
import com.m2ibank.common.exception.ResourceNotFoundException;
import com.m2ibank.customer.dto.CustomerRequest;
import com.m2ibank.customer.dto.CustomerResponse;
import com.m2ibank.customer.entity.Customer;
import com.m2ibank.customer.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    // Generic message avoids revealing which field (email/phone) already exists.
    private static final String DUPLICATE_CUSTOMER_MESSAGE = "A customer with these details already exists";
    private static final String CUSTOMER_NOT_FOUND_MESSAGE = "Customer not found";

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerResponse createCustomer(CustomerRequest request) {
        customerRepository.findByEmail(request.getEmail())
            .ifPresent(c -> {
                log.info("Customer creation rejected: email already registered (customerId={})", c.getId());
                throw new BusinessException(DUPLICATE_CUSTOMER_MESSAGE);
            });

        customerRepository.findByPhoneNumber(request.getPhoneNumber())
            .ifPresent(c -> {
                log.info("Customer creation rejected: phone number already registered (customerId={})", c.getId());
                throw new BusinessException(DUPLICATE_CUSTOMER_MESSAGE);
            });

        Customer customer = new Customer(
            request.getFullName(),
            request.getEmail(),
            request.getPhoneNumber(),
            request.getNationalId()
        );

        Customer savedCustomer = customerRepository.save(customer);
        return mapToResponse(savedCustomer);
    }

    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> {
                log.info("Customer lookup failed: no customer with id={}", id);
                return new ResourceNotFoundException(CUSTOMER_NOT_FOUND_MESSAGE);
            });
        return mapToResponse(customer);
    }

    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
            .map(this::mapToResponse)
            .toList();
    }

    public boolean existsByEmail(String email) {
        return customerRepository.findByEmail(email).isPresent();
    }

    public CustomerResponse getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.info("Customer lookup failed: no customer with the given email");
                return new ResourceNotFoundException(CUSTOMER_NOT_FOUND_MESSAGE);
            });
        return mapToResponse(customer);
    }

    public Customer getCustomerEntityById(Long id) {
        return customerRepository.findById(id)
            .orElseThrow(() -> {
                log.info("Customer lookup failed: no customer with id={}", id);
                return new ResourceNotFoundException(CUSTOMER_NOT_FOUND_MESSAGE);
            });
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return new CustomerResponse(
            customer.getId(),
            customer.getFullName(),
            customer.getEmail(),
            customer.getPhoneNumber(),
            customer.getCreatedAt()
        );
    }
}

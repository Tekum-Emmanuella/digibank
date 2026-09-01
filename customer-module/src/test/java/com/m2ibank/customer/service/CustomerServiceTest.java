package com.m2ibank.customer.service;

import com.m2ibank.common.exception.BusinessException;
import com.m2ibank.common.exception.ResourceNotFoundException;
import com.m2ibank.customer.dto.CustomerRequest;
import com.m2ibank.customer.dto.CustomerResponse;
import com.m2ibank.customer.entity.Customer;
import com.m2ibank.customer.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void createCustomer_Success() {
        // Given
        CustomerRequest request = new CustomerRequest();
        request.setFullName("John Doe");
        request.setEmail("john.doe@example.com");
        request.setPhoneNumber("+1234567890");
        request.setNationalId("ID123456");

        Customer savedCustomer = new Customer("John Doe", "john.doe@example.com", "+1234567890", "ID123456");
        savedCustomer.setId(1L);
        savedCustomer.setCreatedAt(LocalDateTime.now());

        when(customerRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(customerRepository.findByPhoneNumber(request.getPhoneNumber())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        // When
        CustomerResponse response = customerService.createCustomer(request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John Doe", response.getFullName());
        assertEquals("john.doe@example.com", response.getEmail());
        assertEquals("+1234567890", response.getPhoneNumber());
        assertEquals("ID123456", response.getNationalId());
        assertNotNull(response.getCreatedAt());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void createCustomer_DuplicateEmail_ThrowsException() {
        // Given
        CustomerRequest request = new CustomerRequest();
        request.setFullName("John Doe");
        request.setEmail("existing@example.com");
        request.setPhoneNumber("+1234567890");
        request.setNationalId("ID123456");

        Customer existingCustomer = new Customer("Existing User", "existing@example.com", "+9999999999", "ID999999");
        existingCustomer.setId(1L);

        when(customerRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(existingCustomer));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            customerService.createCustomer(request);
        });
        assertEquals("A customer with this email already exists", exception.getMessage());
    }

    @Test
    void createCustomer_DuplicatePhoneNumber_ThrowsException() {
        // Given
        CustomerRequest request = new CustomerRequest();
        request.setFullName("John Doe");
        request.setEmail("john.doe@example.com");
        request.setPhoneNumber("+1234567890");
        request.setNationalId("ID123456");

        Customer existingCustomer = new Customer("Existing User", "other@example.com", "+1234567890", "ID999999");
        existingCustomer.setId(1L);

        when(customerRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(customerRepository.findByPhoneNumber(request.getPhoneNumber())).thenReturn(Optional.of(existingCustomer));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            customerService.createCustomer(request);
        });
        assertEquals("A customer with this phone number already exists", exception.getMessage());
    }

    @Test
    void getCustomerById_NotFound_ThrowsException() {
        // Given
        Long customerId = 999L;
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            customerService.getCustomerById(customerId);
        });
        assertTrue(exception.getMessage().contains("Customer not found with id 999"));
    }

    @Test
    void getAllCustomers_Success() {
        // Given
        Customer customer1 = new Customer("John Doe", "john@example.com", "+1111111111", "ID111");
        customer1.setId(1L);
        customer1.setCreatedAt(LocalDateTime.now());

        Customer customer2 = new Customer("Jane Doe", "jane@example.com", "+2222222222", "ID222");
        customer2.setId(2L);
        customer2.setCreatedAt(LocalDateTime.now());

        List<Customer> customers = Arrays.asList(customer1, customer2);
        when(customerRepository.findAll()).thenReturn(customers);

        // When
        List<CustomerResponse> responses = customerService.getAllCustomers();

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        
        CustomerResponse response1 = responses.get(0);
        assertEquals(1L, response1.getId());
        assertEquals("John Doe", response1.getFullName());
        assertEquals("john@example.com", response1.getEmail());

        CustomerResponse response2 = responses.get(1);
        assertEquals(2L, response2.getId());
        assertEquals("Jane Doe", response2.getFullName());
        assertEquals("jane@example.com", response2.getEmail());
    }
}

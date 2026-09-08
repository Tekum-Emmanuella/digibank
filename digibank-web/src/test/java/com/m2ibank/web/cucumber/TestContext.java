package com.m2ibank.web.cucumber;

import com.m2ibank.customer.entity.Customer;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TestContext {
    
    private ResponseEntity<String> response;
    private Customer currentCustomer;
    
    public ResponseEntity<String> getResponse() {
        return response;
    }
    
    public void setResponse(ResponseEntity<String> response) {
        this.response = response;
    }

    public Customer getCurrentCustomer() {
        return currentCustomer;
    }

    public void setCurrentCustomer(Customer currentCustomer) {
        this.currentCustomer = currentCustomer;
    }
}

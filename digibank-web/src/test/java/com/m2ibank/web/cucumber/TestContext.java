package com.m2ibank.web.cucumber;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TestContext {
    
    private ResponseEntity<String> response;
    
    public ResponseEntity<String> getResponse() {
        return response;
    }
    
    public void setResponse(ResponseEntity<String> response) {
        this.response = response;
    }
}

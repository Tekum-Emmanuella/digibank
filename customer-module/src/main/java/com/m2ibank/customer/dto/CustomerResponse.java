package com.m2ibank.customer.dto;

import java.time.LocalDateTime;

/**
 * Workshop 2 remediation (SAST §3.5.2/§4.4): the response no longer exposes
 * {@code nationalId}. Returning a customer's national identifier to every API consumer by
 * default is excessive data exposure (data minimization principle); this field should only
 * ever be surfaced through a future, authorization-aware endpoint if a legitimate business
 * need arises.
 */
public class CustomerResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private LocalDateTime createdAt;
 
    // Required for JSON deserialization and JPA operations
    public CustomerResponse() {
    }

    public CustomerResponse(Long id, String fullName, String email, String phoneNumber, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

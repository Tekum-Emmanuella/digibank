package com.m2ibank.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Workshop 2 remediation (SAST §4.3): the original DTO relied only on {@code @Size} for
 * phoneNumber/nationalId, which bounds length but not shape, allowing arbitrary characters
 * to reach the business layer. Each sensitive field now carries an explicit {@code @Pattern}
 * constraint, and setters normalize input (trim/case) so validation is not bypassed by
 * incidental whitespace or casing.
 */
public class CustomerRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 120, message = "Full name must contain between 3 and 120 characters")
    @Pattern(regexp = "^[A-Za-zÀ-ÿ'\\-\\s]+$", message = "Full name contains invalid characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Phone number format is invalid")
    private String phoneNumber;

    @NotBlank(message = "National ID is required")
    @Pattern(regexp = "^[A-Z0-9_-]{5,30}$", message = "National ID format is invalid")
    private String nationalId;
 
    // Required for JSON deserialization and JPA operations
    public CustomerRequest() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName == null ? null : fullName.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim().toLowerCase();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber == null ? null : phoneNumber.trim();
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId == null ? null : nationalId.trim().toUpperCase();
    }
}

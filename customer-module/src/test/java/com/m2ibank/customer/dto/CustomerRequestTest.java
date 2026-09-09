package com.m2ibank.customer.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Workshop 2 remediation coverage: verifies the normalization behaviour added to the
 * setters (trim/case handling) that hardens validation against whitespace/casing bypass,
 * and guards against null input causing a NullPointerException instead of a clean null.
 */
class CustomerRequestTest {

    @Test
    void shouldTrimFullName() {
        CustomerRequest request = new CustomerRequest();

        request.setFullName("  John Doe  ");

        assertEquals("John Doe", request.getFullName());
    }

    @Test
    void shouldAllowNullFullName() {
        CustomerRequest request = new CustomerRequest();

        request.setFullName(null);

        assertNull(request.getFullName());
    }

    @Test
    void shouldTrimAndLowercaseEmail() {
        CustomerRequest request = new CustomerRequest();

        request.setEmail("  John.Doe@Example.COM  ");

        assertEquals("john.doe@example.com", request.getEmail());
    }

    @Test
    void shouldAllowNullEmail() {
        CustomerRequest request = new CustomerRequest();

        request.setEmail(null);

        assertNull(request.getEmail());
    }

    @Test
    void shouldTrimPhoneNumber() {
        CustomerRequest request = new CustomerRequest();

        request.setPhoneNumber("  +237600000003  ");

        assertEquals("+237600000003", request.getPhoneNumber());
    }

    @Test
    void shouldAllowNullPhoneNumber() {
        CustomerRequest request = new CustomerRequest();

        request.setPhoneNumber(null);

        assertNull(request.getPhoneNumber());
    }

    @Test
    void shouldTrimAndUppercaseNationalId() {
        CustomerRequest request = new CustomerRequest();

        request.setNationalId("  cni000003  ");

        assertEquals("CNI000003", request.getNationalId());
    }

    @Test
    void shouldAllowNullNationalId() {
        CustomerRequest request = new CustomerRequest();

        request.setNationalId(null);

        assertNull(request.getNationalId());
    }
}

package com.m2ibank.transfer.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransferRequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void shouldPassValidationForValidRequest() {
        TransferRequest request = new TransferRequest();
        request.setSourceAccountId(1L);
        request.setDestinationAccountId(2L);
        request.setAmount(new BigDecimal("5000.00"));
        request.setDescription("Salary transfer");

        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldPassValidationWhenDescriptionIsNull() {
        TransferRequest request = new TransferRequest();
        request.setSourceAccountId(1L);
        request.setDestinationAccountId(2L);
        request.setAmount(new BigDecimal("5000.00"));

        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenSourceAccountIdIsMissing() {
        TransferRequest request = new TransferRequest();
        request.setDestinationAccountId(2L);
        request.setAmount(new BigDecimal("5000.00"));

        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("Source account ID is required", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailValidationWhenDestinationAccountIdIsMissing() {
        TransferRequest request = new TransferRequest();
        request.setSourceAccountId(1L);
        request.setAmount(new BigDecimal("5000.00"));

        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("Destination account ID is required", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailValidationWhenAmountIsMissing() {
        TransferRequest request = new TransferRequest();
        request.setSourceAccountId(1L);
        request.setDestinationAccountId(2L);

        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("Amount is required", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailValidationWhenAmountIsZero() {
        TransferRequest request = new TransferRequest();
        request.setSourceAccountId(1L);
        request.setDestinationAccountId(2L);
        request.setAmount(new BigDecimal("0.00"));

        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("Transfer amount must be greater than zero", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailValidationWhenAmountIsNegative() {
        TransferRequest request = new TransferRequest();
        request.setSourceAccountId(1L);
        request.setDestinationAccountId(2L);
        request.setAmount(new BigDecimal("-100.00"));

        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("Transfer amount must be greater than zero", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailValidationWhenDescriptionIsTooLong() {
        TransferRequest request = new TransferRequest();
        request.setSourceAccountId(1L);
        request.setDestinationAccountId(2L);
        request.setAmount(new BigDecimal("5000.00"));
        request.setDescription("d".repeat(256));

        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("Description must not exceed 255 characters", violations.iterator().next().getMessage());
    }
}

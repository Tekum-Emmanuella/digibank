package com.m2ibank.account.dto;

import com.m2ibank.account.entity.AccountType;
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

class AccountRequestValidationTest {

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
    void shouldPassValidationForValidAccountRequest() {
        AccountRequest request = validRequest();

        Set<ConstraintViolation<AccountRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenCustomerIdIsNull() {
        AccountRequest request = validRequest();
        request.setCustomerId(null);

        Set<ConstraintViolation<AccountRequest>> violations = validator.validate(request);

        assertSingleViolation(violations, "Customer ID is required");
    }

    @Test
    void shouldFailValidationWhenAccountTypeIsNull() {
        AccountRequest request = validRequest();
        request.setAccountType(null);

        Set<ConstraintViolation<AccountRequest>> violations = validator.validate(request);

        assertSingleViolation(violations, "Account type is required");
    }

    @Test
    void shouldFailValidationWhenInitialBalanceIsNull() {
        AccountRequest request = validRequest();
        request.setInitialBalance(null);

        Set<ConstraintViolation<AccountRequest>> violations = validator.validate(request);

        assertSingleViolation(violations, "Initial balance is required");
    }

    @Test
    void shouldFailValidationWhenInitialBalanceIsNegative() {
        AccountRequest request = validRequest();
        request.setInitialBalance(new BigDecimal("-0.01"));

        Set<ConstraintViolation<AccountRequest>> violations = validator.validate(request);

        assertSingleViolation(violations, "Initial balance must be greater than or equal to zero");
    }

    @Test
    void shouldFailValidationWhenInitialBalanceHasTooManyDecimalPlaces() {
        AccountRequest request = validRequest();
        request.setInitialBalance(new BigDecimal("100.999"));

        Set<ConstraintViolation<AccountRequest>> violations = validator.validate(request);

        assertSingleViolation(violations, "Initial balance must have at most 17 integer digits and 2 decimals");
    }

    private AccountRequest validRequest() {
        AccountRequest request = new AccountRequest();
        request.setCustomerId(1L);
        request.setAccountType(AccountType.CURRENT);
        request.setInitialBalance(new BigDecimal("100000.00"));
        return request;
    }

    private void assertSingleViolation(Set<ConstraintViolation<AccountRequest>> violations, String expectedMessage) {
        assertEquals(1, violations.size());
        assertEquals(expectedMessage, violations.iterator().next().getMessage());
    }
}

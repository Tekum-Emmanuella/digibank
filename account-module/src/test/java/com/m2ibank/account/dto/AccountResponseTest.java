package com.m2ibank.account.dto;

import com.m2ibank.account.entity.AccountType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountResponseTest {

    @Test
    void shouldExposeConstructorValues() {
        LocalDateTime createdAt = LocalDateTime.of(2026, Month.AUGUST, 28, 10, 15);

        AccountResponse response = new AccountResponse(
                1L,
                "DB-00000001",
                new BigDecimal("250000.00"),
                AccountType.SAVINGS,
                10L,
                createdAt
        );

        assertEquals(1L, response.getId());
        assertEquals("DB-00000001", response.getAccountNumber());
        assertEquals(new BigDecimal("250000.00"), response.getBalance());
        assertEquals(AccountType.SAVINGS, response.getAccountType());
        assertEquals(10L, response.getCustomerId());
        assertEquals(createdAt, response.getCreatedAt());
    }
}

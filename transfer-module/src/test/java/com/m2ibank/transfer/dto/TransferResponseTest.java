package com.m2ibank.transfer.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransferResponseTest {

    @Test
    void shouldCreateTransferResponseWithConstructorValues() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 27, 10, 30);
        TransferResponse response = new TransferResponse(
                1L,
                10L,
                20L,
                new BigDecimal("5000.00"),
                "Salary transfer",
                createdAt
        );

        assertEquals(1L, response.getId());
        assertEquals(10L, response.getSourceAccountId());
        assertEquals(20L, response.getDestinationAccountId());
        assertEquals(new BigDecimal("5000.00"), response.getAmount());
        assertEquals("Salary transfer", response.getDescription());
        assertEquals(createdAt, response.getCreatedAt());
    }
}

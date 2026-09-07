package com.m2ibank.account.dto;

import com.m2ibank.account.entity.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountResponse {

    private final Long id;
    private final String accountNumber;
    private final BigDecimal balance;
    private final AccountType accountType;
    private final Long customerId;
    private final LocalDateTime createdAt;

    public AccountResponse(
            Long id,
            String accountNumber,
            BigDecimal balance,
            AccountType accountType,
            Long customerId,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.accountType = accountType;
        this.customerId = customerId;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

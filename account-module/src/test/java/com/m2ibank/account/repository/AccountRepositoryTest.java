package com.m2ibank.account.repository;

import com.m2ibank.account.entity.Account;
import com.m2ibank.account.entity.AccountType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void shouldSaveAccount() {
        Account account = new Account(
                "DB-1001",
                new BigDecimal("100000.00"),
                AccountType.CURRENT,
                1L
        );

        Account savedAccount = accountRepository.save(account);

        assertNotNull(savedAccount.getId());
        assertEquals("DB-1001", savedAccount.getAccountNumber());
        assertEquals(new BigDecimal("100000.00"), savedAccount.getBalance());
        assertEquals(AccountType.CURRENT, savedAccount.getAccountType());
        assertEquals(1L, savedAccount.getCustomerId());
        assertNotNull(savedAccount.getCreatedAt());
    }

    @Test
    void shouldFindAccountByAccountNumber() {
        Account account = new Account(
                "DB-1002",
                new BigDecimal("75000.00"),
                AccountType.SAVINGS,
                2L
        );
        accountRepository.save(account);

        Optional<Account> foundAccount = accountRepository.findByAccountNumber("DB-1002");

        assertTrue(foundAccount.isPresent());
        assertEquals("DB-1002", foundAccount.get().getAccountNumber());
        assertEquals(new BigDecimal("75000.00"), foundAccount.get().getBalance());
        assertEquals(AccountType.SAVINGS, foundAccount.get().getAccountType());
        assertEquals(2L, foundAccount.get().getCustomerId());
    }

    @Test
    void shouldFindAccountsByCustomerId() {
        Account currentAccount = new Account(
                "DB-1003",
                new BigDecimal("50000.00"),
                AccountType.CURRENT,
                3L
        );
        Account savingsAccount = new Account(
                "DB-1004",
                new BigDecimal("125000.00"),
                AccountType.SAVINGS,
                3L
        );
        Account anotherCustomerAccount = new Account(
                "DB-1005",
                new BigDecimal("90000.00"),
                AccountType.CURRENT,
                4L
        );
        accountRepository.saveAll(List.of(currentAccount, savingsAccount, anotherCustomerAccount));

        List<Account> customerAccounts = accountRepository.findByCustomerId(3L);

        assertEquals(2, customerAccounts.size());
        assertTrue(customerAccounts.stream().allMatch(account -> account.getCustomerId().equals(3L)));
    }

    @Test
    void shouldReturnEmptyWhenAccountNumberDoesNotExist() {
        Optional<Account> foundAccount = accountRepository.findByAccountNumber("DB-9999");

        assertFalse(foundAccount.isPresent());
    }
}

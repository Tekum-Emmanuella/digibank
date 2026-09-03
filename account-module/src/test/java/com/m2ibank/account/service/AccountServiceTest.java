package com.m2ibank.account.service;

import com.m2ibank.account.dto.AccountRequest;
import com.m2ibank.account.dto.AccountResponse;
import com.m2ibank.account.entity.Account;
import com.m2ibank.account.entity.AccountType;
import com.m2ibank.account.repository.AccountRepository;
import com.m2ibank.common.exception.BusinessException;
import com.m2ibank.common.exception.ResourceNotFoundException;
import com.m2ibank.customer.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerService customerService;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, customerService, () -> "DB-00000001");
    }

    @Test
    void shouldCreateAccountSuccessfully() throws Exception {
        AccountRequest request = accountRequest();
        when(accountRepository.findByAccountNumber("DB-00000001")).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            setField(account, "id", 1L);
            setField(account, "createdAt", LocalDateTime.of(2026, Month.AUGUST, 28, 9, 0));
            return account;
        });

        AccountResponse response = accountService.createAccount(request);

        assertEquals(1L, response.getId());
        assertEquals("DB-00000001", response.getAccountNumber());
        assertEquals(request.getInitialBalance(), response.getBalance());
        assertEquals(request.getAccountType(), response.getAccountType());
        assertEquals(request.getCustomerId(), response.getCustomerId());
        assertNotNull(response.getCreatedAt());

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());
        assertEquals("DB-00000001", accountCaptor.getValue().getAccountNumber());
        assertEquals(new BigDecimal("100000.00"), accountCaptor.getValue().getBalance());
        verify(customerService).getCustomerEntityById(1L);
    }

    @Test
    void shouldGenerateAnotherAccountNumberWhenFirstNumberAlreadyExists() {
        AtomicInteger counter = new AtomicInteger();
        Supplier<String> accountNumberSupplier = () -> counter.getAndIncrement() == 0
                ? "DB-00000001"
                : "DB-00000002";
        accountService = new AccountService(accountRepository, customerService, accountNumberSupplier);

        when(accountRepository.findByAccountNumber("DB-00000001")).thenReturn(Optional.of(existingAccount()));
        when(accountRepository.findByAccountNumber("DB-00000002")).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponse response = accountService.createAccount(accountRequest());

        assertEquals("DB-00000002", response.getAccountNumber());
        verify(accountRepository).findByAccountNumber("DB-00000001");
        verify(accountRepository).findByAccountNumber("DB-00000002");
        verify(customerService).getCustomerEntityById(1L);
    }

    @Test
    void shouldThrowExceptionWhenUniqueAccountNumberCannotBeGenerated() {
        AccountRequest request = accountRequest();
        when(accountRepository.findByAccountNumber(anyString())).thenReturn(Optional.of(existingAccount()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> accountService.createAccount(request)
        );

        assertEquals("Unable to generate a unique account number", exception.getMessage());
        verify(accountRepository, times(10)).findByAccountNumber("DB-00000001");
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenCreatingAccountForUnknownCustomer() {
        AccountRequest request = accountRequest();
        when(customerService.getCustomerEntityById(1L))
                .thenThrow(new ResourceNotFoundException("Customer not found with id 1"));

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.createAccount(request)
        );

        assertEquals("Customer not found with id 1", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
        verify(accountRepository, never()).findByAccountNumber(anyString());
    }

    @Test
    void shouldReturnAccountById() throws Exception {
        Account account = existingAccount();
        setField(account, "id", 1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        AccountResponse response = accountService.getAccountById(1L);

        assertEquals(1L, response.getId());
        assertEquals(account.getAccountNumber(), response.getAccountNumber());
        assertEquals(account.getBalance(), response.getBalance());
        assertEquals(account.getAccountType(), response.getAccountType());
        assertEquals(account.getCustomerId(), response.getCustomerId());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenAccountDoesNotExist() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.getAccountById(99L)
        );

        assertEquals("Account not found with id: 99", exception.getMessage());
    }

    @Test
    void shouldReturnAccountEntityById() {
        Account account = existingAccount();
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        Account foundAccount = accountService.getAccountEntityById(1L);

        assertSame(account, foundAccount);
    }

    @Test
    void shouldReturnAccountsByCustomerId() {
        Account currentAccount = new Account("DB-00000003", new BigDecimal("50000.00"), AccountType.CURRENT, 2L);
        Account savingsAccount = new Account("DB-00000004", new BigDecimal("150000.00"), AccountType.SAVINGS, 2L);
        when(accountRepository.findByCustomerId(2L)).thenReturn(List.of(currentAccount, savingsAccount));

        List<AccountResponse> accounts = accountService.getAccountsByCustomerId(2L);

        assertEquals(2, accounts.size());
        assertTrue(accounts.stream().allMatch(account -> account.getCustomerId().equals(2L)));
        verify(customerService).getCustomerEntityById(2L);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenListingAccountsForUnknownCustomer() {
        when(customerService.getCustomerEntityById(9L))
                .thenThrow(new ResourceNotFoundException("Customer not found with id 9"));

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.getAccountsByCustomerId(9L)
        );

        assertEquals("Customer not found with id 9", exception.getMessage());
        verify(accountRepository, never()).findByCustomerId(eq(9L));
    }

    @Test
    void shouldDebitAccountSuccessfully() {
        Account account = existingAccount();
        when(accountRepository.save(account)).thenReturn(account);

        accountService.debitAccount(account, new BigDecimal("25000.00"));

        assertEquals(new BigDecimal("75000.00"), account.getBalance());
        verify(accountRepository).save(account);
    }

    @Test
    void shouldRejectDebitWhenBalanceIsInsufficient() {
        Account account = existingAccount();
        BigDecimal debitAmount = new BigDecimal("100000.01");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> accountService.debitAccount(account, debitAmount)
        );

        assertEquals("Insufficient balance for transfer", exception.getMessage());
        assertEquals(new BigDecimal("100000.00"), account.getBalance());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void shouldCreditAccountSuccessfully() {
        Account account = existingAccount();
        when(accountRepository.save(account)).thenReturn(account);

        accountService.creditAccount(account, new BigDecimal("5000.00"));

        assertEquals(new BigDecimal("105000.00"), account.getBalance());
        verify(accountRepository).save(account);
    }

    @Test
    void shouldRejectDebitWhenAmountIsZeroOrNegative() {
        Account account = existingAccount();
        BigDecimal negativeAmount = new BigDecimal("-1.00");

        assertThrows(BusinessException.class, () -> accountService.debitAccount(account, BigDecimal.ZERO));
        assertThrows(BusinessException.class, () -> accountService.debitAccount(account, negativeAmount));

        assertEquals(new BigDecimal("100000.00"), account.getBalance());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void shouldRejectCreditWhenAmountIsZeroOrNegative() {
        Account account = existingAccount();
        BigDecimal negativeAmount = new BigDecimal("-1.00");

        assertThrows(BusinessException.class, () -> accountService.creditAccount(account, BigDecimal.ZERO));
        assertThrows(BusinessException.class, () -> accountService.creditAccount(account, negativeAmount));

        assertEquals(new BigDecimal("100000.00"), account.getBalance());
        verify(accountRepository, never()).save(any(Account.class));
    }

    private AccountRequest accountRequest() {
        AccountRequest request = new AccountRequest();
        request.setCustomerId(1L);
        request.setAccountType(AccountType.CURRENT);
        request.setInitialBalance(new BigDecimal("100000.00"));
        return request;
    }

    private Account existingAccount() {
        return new Account("DB-00000001", new BigDecimal("100000.00"), AccountType.CURRENT, 1L);
    }

    private void setField(Account account, String fieldName, Object value) throws Exception {
        Field field = Account.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(account, value);
    }
}

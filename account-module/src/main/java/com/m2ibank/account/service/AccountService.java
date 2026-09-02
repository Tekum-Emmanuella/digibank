package com.m2ibank.account.service;

import com.m2ibank.account.dto.AccountRequest;
import com.m2ibank.account.dto.AccountResponse;
import com.m2ibank.account.entity.Account;
import com.m2ibank.account.repository.AccountRepository;
import com.m2ibank.common.exception.BusinessException;
import com.m2ibank.common.exception.ResourceNotFoundException;
import com.m2ibank.customer.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Service
public class AccountService {

    private static final int ACCOUNT_NUMBER_BOUND = 100_000_000;
    private static final int MAX_ACCOUNT_NUMBER_ATTEMPTS = 10;

    private final AccountRepository accountRepository;
    private final CustomerService customerService;
    private final Supplier<String> accountNumberSupplier;

    @Autowired
    public AccountService(AccountRepository accountRepository, CustomerService customerService) {
        this(accountRepository, customerService, new SecureRandomAccountNumberSupplier());
    }

    AccountService(AccountRepository accountRepository, CustomerService customerService, Supplier<String> accountNumberSupplier) {
        this.accountRepository = Objects.requireNonNull(accountRepository, "accountRepository must not be null");
        this.customerService = Objects.requireNonNull(customerService, "customerService must not be null");
        this.accountNumberSupplier = Objects.requireNonNull(accountNumberSupplier, "accountNumberSupplier must not be null");
    }

    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        customerService.getCustomerEntityById(request.getCustomerId());

        String accountNumber = generateUniqueAccountNumber();
        Account account = new Account(
                accountNumber,
                request.getInitialBalance(),
                request.getAccountType(),
                request.getCustomerId()
        );

        return mapToResponse(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id) {
        return mapToResponse(findAccountById(id));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByCustomerId(Long customerId) {
        customerService.getCustomerEntityById(customerId);
        return accountRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Account getAccountEntityById(Long id) {
        return findAccountById(id);
    }

    private Account findAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
    }

    @Transactional
    public void debitAccount(Account account, BigDecimal amount) {
        validateAccount(account);
        validatePositiveAmount(amount);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient balance for transfer");
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
    }

    @Transactional
    public void creditAccount(Account account, BigDecimal amount) {
        validateAccount(account);
        validatePositiveAmount(amount);

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
    }

    private String generateUniqueAccountNumber() {
        for (int attempt = 0; attempt < MAX_ACCOUNT_NUMBER_ATTEMPTS; attempt++) {
            String accountNumber = accountNumberSupplier.get();
            if (accountRepository.findByAccountNumber(accountNumber).isEmpty()) {
                return accountNumber;
            }
        }

        throw new IllegalStateException("Unable to generate a unique account number");
    }

    private AccountResponse mapToResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getAccountType(),
                account.getCustomerId(),
                account.getCreatedAt()
        );
    }

    private void validateAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account is required");
        }
        if (account.getBalance() == null) {
            throw new IllegalArgumentException("Account balance is required");
        }
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount is required");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    private static final class SecureRandomAccountNumberSupplier implements Supplier<String> {

        private final SecureRandom secureRandom = new SecureRandom();

        @Override
        public String get() {
            return "DB-%08d".formatted(secureRandom.nextInt(ACCOUNT_NUMBER_BOUND));
        }
    }
}

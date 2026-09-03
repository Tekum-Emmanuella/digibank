package com.m2ibank.account.service;

import com.m2ibank.account.entity.Account;
import com.m2ibank.account.repository.AccountRepository;
import com.m2ibank.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account getAccountEntityById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Account not found with id " + id));
    }

    public void debitAccount(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient balance for transfer");
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
    }

    public void creditAccount(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
    }
}

package com.m2ibank.web.cucumber;

import com.m2ibank.account.repository.AccountRepository;
import com.m2ibank.customer.repository.CustomerRepository;
import com.m2ibank.transfer.repository.TransferRepository;
import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Ensures each Cucumber scenario starts with a clean database.
 * The Spring context (and H2 in-memory database) is shared across all
 * scenarios in a test run, so without this cleanup, data created in one
 * scenario (e.g. customers with fixed emails) would collide with data in
 * subsequent scenarios.
 */
public class CucumberHooks {

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Before
    public void cleanDatabase() {
        // Delete in FK-safe order: transfers -> accounts -> customers
        transferRepository.deleteAll();
        accountRepository.deleteAll();
        customerRepository.deleteAll();
    }
}

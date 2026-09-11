package com.m2ibank.web.bootstrap;

import com.m2ibank.account.dto.AccountRequest;
import com.m2ibank.account.entity.AccountType;
import com.m2ibank.account.service.AccountService;
import com.m2ibank.customer.dto.CustomerRequest;
import com.m2ibank.customer.dto.CustomerResponse;
import com.m2ibank.customer.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Seeds demonstration customers and accounts for local/educational use. Idempotent by
 * demo email. Disabled by default (Flyway's V2__insert_seed_data.sql owns demo data
 * seeding); enable with digibank.demo-data.enabled=true for ad hoc local demonstrations.
 */
@Component
@ConditionalOnProperty(name = "digibank.demo-data.enabled", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final String DEMO_ALICE_EMAIL = "alice@m2ibank.com";
    private static final String DEMO_BRIAN_EMAIL = "brian@m2ibank.com";

    private final CustomerService customerService;
    private final AccountService accountService;

    public DataInitializer(CustomerService customerService, AccountService accountService) {
        this.customerService = customerService;
        this.accountService = accountService;
    }

    @Override
    public void run(String... args) {
        boolean aliceExists = customerService.existsByEmail(DEMO_ALICE_EMAIL);
        boolean brianExists = customerService.existsByEmail(DEMO_BRIAN_EMAIL);

        if (aliceExists && brianExists) {
            log.info("Demo data seeding skipped: demo customers already exist ({}, {})",
                    DEMO_ALICE_EMAIL, DEMO_BRIAN_EMAIL);
            ensureDemoAccounts();
            return;
        }

        log.info("Seeding DigiBank demonstration customers and accounts");

        CustomerResponse alice = aliceExists
                ? customerService.getCustomerByEmail(DEMO_ALICE_EMAIL)
                : createCustomer("Alice Ndzi", DEMO_ALICE_EMAIL, "+237600000001", "CNI000001");

        CustomerResponse brian = brianExists
                ? customerService.getCustomerByEmail(DEMO_BRIAN_EMAIL)
                : createCustomer("Brian Tchoumi", DEMO_BRIAN_EMAIL, "+237600000002", "CNI000002");

        ensureAccount(alice.getId(), AccountType.CURRENT, new BigDecimal("150000.00"));
        ensureAccount(brian.getId(), AccountType.SAVINGS, new BigDecimal("90000.00"));

        log.info(
                "Demo data ready: customers id={} (Alice), id={} (Brian) with CURRENT and SAVINGS accounts",
                alice.getId(),
                brian.getId()
        );
    }

    private void ensureDemoAccounts() {
        CustomerResponse alice = customerService.getCustomerByEmail(DEMO_ALICE_EMAIL);
        CustomerResponse brian = customerService.getCustomerByEmail(DEMO_BRIAN_EMAIL);
        ensureAccount(alice.getId(), AccountType.CURRENT, new BigDecimal("150000.00"));
        ensureAccount(brian.getId(), AccountType.SAVINGS, new BigDecimal("90000.00"));
    }

    private CustomerResponse createCustomer(String fullName, String email, String phoneNumber, String nationalId) {
        CustomerRequest request = new CustomerRequest();
        request.setFullName(fullName);
        request.setEmail(email);
        request.setPhoneNumber(phoneNumber);
        request.setNationalId(nationalId);
        return customerService.createCustomer(request);
    }

    private void ensureAccount(Long customerId, AccountType accountType, BigDecimal initialBalance) {
        if (!accountService.getAccountsByCustomerId(customerId).isEmpty()) {
            return;
        }
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setCustomerId(customerId);
        accountRequest.setAccountType(accountType);
        accountRequest.setInitialBalance(initialBalance);
        accountService.createAccount(accountRequest);
    }
}

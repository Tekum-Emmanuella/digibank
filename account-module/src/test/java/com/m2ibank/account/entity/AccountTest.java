package com.m2ibank.account.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountTest {

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
    void shouldCreateAccountWithConstructorValues() {
        Account account = new Account(
                "DB-0001",
                new BigDecimal("100000.00"),
                AccountType.CURRENT,
                1L
        );

        assertEquals("DB-0001", account.getAccountNumber());
        assertEquals(new BigDecimal("100000.00"), account.getBalance());
        assertEquals(AccountType.CURRENT, account.getAccountType());
        assertEquals(1L, account.getCustomerId());
    }

    @Test
    void shouldSetAndGetAccountFields() {
        Account account = new Account();

        account.setAccountNumber("DB-0002");
        account.setBalance(new BigDecimal("50000.00"));
        account.setAccountType(AccountType.SAVINGS);
        account.setCustomerId(2L);

        assertEquals("DB-0002", account.getAccountNumber());
        assertEquals(new BigDecimal("50000.00"), account.getBalance());
        assertEquals(AccountType.SAVINGS, account.getAccountType());
        assertEquals(2L, account.getCustomerId());
    }

    @Test
    void shouldContainExpectedAccountTypes() {
        AccountType[] accountTypes = AccountType.values();

        assertEquals(2, accountTypes.length);
        assertTrue(contains(accountTypes, AccountType.CURRENT));
        assertTrue(contains(accountTypes, AccountType.SAVINGS));
    }

    @Test
    void shouldSetCreatedAtBeforePersisting() {
        Account account = new Account(
                "DB-0003",
                new BigDecimal("75000.00"),
                AccountType.CURRENT,
                3L
        );

        account.onCreate();

        assertNotNull(account.getCreatedAt());
    }

    @Test
    void shouldMapAccountEntityToAccountsTable() throws Exception {
        Table table = Account.class.getAnnotation(Table.class);
        Field accountTypeField = Account.class.getDeclaredField("accountType");
        Field accountNumberField = Account.class.getDeclaredField("accountNumber");
        Field balanceField = Account.class.getDeclaredField("balance");

        assertNotNull(table);
        assertEquals("accounts", table.name());
        assertEquals("uk_accounts_account_number", table.uniqueConstraints()[0].name());
        assertEquals("account_number", table.uniqueConstraints()[0].columnNames()[0]);

        Enumerated enumerated = accountTypeField.getAnnotation(Enumerated.class);
        assertNotNull(enumerated);
        assertEquals(EnumType.STRING, enumerated.value());

        Column accountNumberColumn = accountNumberField.getAnnotation(Column.class);
        assertEquals("account_number", accountNumberColumn.name());
        assertEquals(50, accountNumberColumn.length());

        Column balanceColumn = balanceField.getAnnotation(Column.class);
        assertEquals(19, balanceColumn.precision());
        assertEquals(2, balanceColumn.scale());
    }

    @Test
    void shouldPassValidationForValidAccount() {
        Account account = new Account(
                "DB-0004",
                new BigDecimal("120000.00"),
                AccountType.SAVINGS,
                4L
        );

        Set<ConstraintViolation<Account>> violations = validator.validate(account);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenRequiredFieldsAreMissing() {
        Account account = new Account();

        Set<ConstraintViolation<Account>> violations = validator.validate(account);

        assertEquals(4, violations.size());
    }

    @Test
    void shouldFailValidationWhenBalanceIsNegative() {
        Account account = new Account(
                "DB-0005",
                new BigDecimal("-1.00"),
                AccountType.CURRENT,
                5L
        );

        Set<ConstraintViolation<Account>> violations = validator.validate(account);

        assertEquals(1, violations.size());
        assertEquals("Balance must not be negative", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailValidationWhenAccountNumberDoesNotUseDigiBankPrefix() {
        Account account = new Account(
                "ACC-0006",
                new BigDecimal("1000.00"),
                AccountType.CURRENT,
                6L
        );

        Set<ConstraintViolation<Account>> violations = validator.validate(account);

        assertEquals(1, violations.size());
        assertEquals("Account number must start with DB-", violations.iterator().next().getMessage());
    }

    private boolean contains(AccountType[] accountTypes, AccountType expected) {
        for (AccountType accountType : accountTypes) {
            if (accountType == expected) {
                return true;
            }
        }
        return false;
    }
}

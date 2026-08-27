package com.m2ibank.transfer.service;

import com.m2ibank.account.entity.Account;
import com.m2ibank.account.entity.AccountType;
import com.m2ibank.account.repository.AccountRepository;
import com.m2ibank.account.service.AccountService;
import com.m2ibank.common.exception.BusinessException;
import com.m2ibank.transfer.dto.TransferRequest;
import com.m2ibank.transfer.dto.TransferResponse;
import com.m2ibank.transfer.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@EnableJpaRepositories(basePackages = {"com.m2ibank.transfer.repository", "com.m2ibank.account.repository"})
@EntityScan(basePackages = {"com.m2ibank.transfer.entity", "com.m2ibank.account.entity"})
class TransferServiceIntegrationTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransferRepository transferRepository;

    private AccountService accountService;
    private TransferService transferService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository);
        transferService = new TransferService(transferRepository, accountService);
    }

    @Test
    void shouldDebitSourceCreditDestinationAndRecordLedger() {
        Account source = accountRepository.save(new Account("DB-SRC12345", new BigDecimal("10000.00"), AccountType.CURRENT, 1L));
        Account destination = accountRepository.save(new Account("DB-DST12345", new BigDecimal("5000.00"), AccountType.SAVINGS, 2L));

        TransferRequest request = new TransferRequest();
        request.setSourceAccountId(source.getId());
        request.setDestinationAccountId(destination.getId());
        request.setAmount(new BigDecimal("2500.00"));
        request.setDescription("Salary transfer");

        TransferResponse response = transferService.createTransfer(request);

        assertNotNull(response.getId());
        assertEquals(source.getId(), response.getSourceAccountId());
        assertEquals(destination.getId(), response.getDestinationAccountId());
        assertEquals(new BigDecimal("2500.00"), response.getAmount());

        Account updatedSource = accountRepository.findById(source.getId()).orElseThrow();
        Account updatedDestination = accountRepository.findById(destination.getId()).orElseThrow();
        assertEquals(new BigDecimal("7500.00"), updatedSource.getBalance());
        assertEquals(new BigDecimal("7500.00"), updatedDestination.getBalance());

        List<com.m2ibank.transfer.entity.Transfer> ledger =
                transferRepository.findBySourceAccountIdOrDestinationAccountId(source.getId(), source.getId());
        assertEquals(1, ledger.size());
        assertEquals(new BigDecimal("2500.00"), ledger.get(0).getAmount());
    }

    @Test
    void shouldRejectSelfTransferWithoutChangingBalances() {
        Account account = accountRepository.save(new Account("DB-SELF123", new BigDecimal("10000.00"), AccountType.CURRENT, 1L));

        TransferRequest request = new TransferRequest();
        request.setSourceAccountId(account.getId());
        request.setDestinationAccountId(account.getId());
        request.setAmount(new BigDecimal("1000.00"));

        assertThrows(BusinessException.class, () -> transferService.createTransfer(request));

        Account unchanged = accountRepository.findById(account.getId()).orElseThrow();
        assertEquals(new BigDecimal("10000.00"), unchanged.getBalance());
        assertEquals(0, transferRepository.count());
    }

    @Test
    void shouldRejectTransferWhenSourceHasInsufficientBalance() {
        Account source = accountRepository.save(new Account("DB-POOR123", new BigDecimal("100.00"), AccountType.CURRENT, 1L));
        Account destination = accountRepository.save(new Account("DB-RICH123", new BigDecimal("5000.00"), AccountType.SAVINGS, 2L));

        TransferRequest request = new TransferRequest();
        request.setSourceAccountId(source.getId());
        request.setDestinationAccountId(destination.getId());
        request.setAmount(new BigDecimal("1000.00"));

        assertThrows(BusinessException.class, () -> transferService.createTransfer(request));

        Account unchangedSource = accountRepository.findById(source.getId()).orElseThrow();
        Account unchangedDestination = accountRepository.findById(destination.getId()).orElseThrow();
        assertEquals(new BigDecimal("100.00"), unchangedSource.getBalance());
        assertEquals(new BigDecimal("5000.00"), unchangedDestination.getBalance());
        assertEquals(0, transferRepository.count());
    }
}

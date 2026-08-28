package com.m2ibank.transfer.service;

import com.m2ibank.account.entity.Account;
import com.m2ibank.account.entity.AccountType;
import com.m2ibank.account.service.AccountService;
import com.m2ibank.common.exception.BusinessException;
import com.m2ibank.transfer.dto.TransferRequest;
import com.m2ibank.transfer.dto.TransferResponse;
import com.m2ibank.transfer.entity.Transfer;
import com.m2ibank.transfer.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private AccountService accountService;

    private TransferService transferService;

    @BeforeEach
    void setUp() {
        transferService = new TransferService(transferRepository, accountService);
    }

    @Test
    void shouldRejectTransferWhenSourceAndDestinationAreTheSame() {
        TransferRequest request = new TransferRequest();
        request.setSourceAccountId(1L);
        request.setDestinationAccountId(1L);
        request.setAmount(new BigDecimal("1000.00"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> transferService.createTransfer(request));

        assertEquals("Source and destination accounts must be different", exception.getMessage());
        verify(accountService, never()).getAccountEntityById(any());
        verify(transferRepository, never()).save(any());
    }

    @Test
    void shouldExecuteTransferAndRecordLedger() {
        Account sourceAccount = new Account("DB-SRC12345", new BigDecimal("10000.00"), AccountType.CURRENT, 1L);
        Account destinationAccount = new Account("DB-DST12345", new BigDecimal("5000.00"), AccountType.SAVINGS, 2L);
        Transfer savedTransfer = new Transfer(1L, 2L, new BigDecimal("2500.00"), "Salary transfer");
        setId(savedTransfer, 99L);

        when(accountService.getAccountEntityById(1L)).thenReturn(sourceAccount);
        when(accountService.getAccountEntityById(2L)).thenReturn(destinationAccount);
        when(transferRepository.save(any(Transfer.class))).thenReturn(savedTransfer);

        TransferRequest request = new TransferRequest();
        request.setSourceAccountId(1L);
        request.setDestinationAccountId(2L);
        request.setAmount(new BigDecimal("2500.00"));
        request.setDescription("Salary transfer");

        TransferResponse response = transferService.createTransfer(request);

        assertNotNull(response);
        assertEquals(99L, response.getId());
        assertEquals(1L, response.getSourceAccountId());
        assertEquals(2L, response.getDestinationAccountId());
        assertEquals(new BigDecimal("2500.00"), response.getAmount());
        assertEquals("Salary transfer", response.getDescription());

        verify(accountService).debitAccount(sourceAccount, new BigDecimal("2500.00"));
        verify(accountService).creditAccount(destinationAccount, new BigDecimal("2500.00"));

        ArgumentCaptor<Transfer> transferCaptor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepository).save(transferCaptor.capture());
        Transfer captured = transferCaptor.getValue();
        assertEquals(1L, captured.getSourceAccountId());
        assertEquals(2L, captured.getDestinationAccountId());
        assertEquals(new BigDecimal("2500.00"), captured.getAmount());
        assertEquals("Salary transfer", captured.getDescription());
    }

    @Test
    void shouldGetTransfersForAccount() {
        Transfer transfer = new Transfer(10L, 20L, new BigDecimal("1000.00"), "Payment");
        setId(transfer, 5L);

        when(accountService.getAccountEntityById(10L)).thenReturn(new Account("DB-ACC", new BigDecimal("0.00"), AccountType.CURRENT, 1L));
        when(transferRepository.findBySourceAccountIdOrDestinationAccountId(10L, 10L))
                .thenReturn(List.of(transfer));

        List<TransferResponse> responses = transferService.getTransfersForAccount(10L);

        assertEquals(1, responses.size());
        TransferResponse response = responses.get(0);
        assertEquals(5L, response.getId());
        assertEquals(10L, response.getSourceAccountId());
        assertEquals(20L, response.getDestinationAccountId());
        assertEquals(new BigDecimal("1000.00"), response.getAmount());
        assertEquals("Payment", response.getDescription());
        verify(accountService).getAccountEntityById(10L);
    }

    @Test
    void shouldReturnEmptyListWhenAccountHasNoTransfers() {
        when(accountService.getAccountEntityById(10L)).thenReturn(new Account("DB-ACC", new BigDecimal("0.00"), AccountType.CURRENT, 1L));
        when(transferRepository.findBySourceAccountIdOrDestinationAccountId(10L, 10L)).thenReturn(List.of());

        List<TransferResponse> responses = transferService.getTransfersForAccount(10L);

        assertEquals(0, responses.size());
    }

    private void setId(Transfer transfer, long id) {
        try {
            Field idField = Transfer.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(transfer, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set transfer id", e);
        }
    }
}

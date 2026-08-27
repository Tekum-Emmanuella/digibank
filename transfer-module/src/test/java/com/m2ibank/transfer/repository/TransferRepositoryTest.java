package com.m2ibank.transfer.repository;

import com.m2ibank.transfer.entity.Transfer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class TransferRepositoryTest {

    @Autowired
    private TransferRepository transferRepository;

    @Test
    void shouldSaveTransfer() {
        Transfer transfer = new Transfer(
                1L,
                2L,
                new BigDecimal("5000.00"),
                "Salary transfer"
        );

        Transfer savedTransfer = transferRepository.save(transfer);

        assertNotNull(savedTransfer.getId());
        assertEquals(1L, savedTransfer.getSourceAccountId());
        assertEquals(2L, savedTransfer.getDestinationAccountId());
        assertEquals(new BigDecimal("5000.00"), savedTransfer.getAmount());
        assertEquals("Salary transfer", savedTransfer.getDescription());
        assertNotNull(savedTransfer.getCreatedAt());
    }

    @Test
    void shouldFindTransfersBySourceAccountId() {
        Transfer transfer = new Transfer(
                10L,
                20L,
                new BigDecimal("1000.00"),
                "Payment"
        );
        transferRepository.save(transfer);

        List<Transfer> transfers = transferRepository.findBySourceAccountIdOrDestinationAccountId(10L, -1L);

        assertEquals(1, transfers.size());
        assertEquals(10L, transfers.get(0).getSourceAccountId());
    }

    @Test
    void shouldFindTransfersByDestinationAccountId() {
        Transfer transfer = new Transfer(
                10L,
                20L,
                new BigDecimal("1000.00"),
                "Payment"
        );
        transferRepository.save(transfer);

        List<Transfer> transfers = transferRepository.findBySourceAccountIdOrDestinationAccountId(-1L, 20L);

        assertEquals(1, transfers.size());
        assertEquals(20L, transfers.get(0).getDestinationAccountId());
    }

    @Test
    void shouldFindTransfersWhereAccountIsSourceOrDestination() {
        Transfer outgoing = new Transfer(
                100L,
                200L,
                new BigDecimal("500.00"),
                "Outgoing"
        );
        Transfer incoming = new Transfer(
                300L,
                100L,
                new BigDecimal("750.00"),
                "Incoming"
        );
        Transfer unrelated = new Transfer(
                400L,
                500L,
                new BigDecimal("900.00"),
                "Unrelated"
        );
        transferRepository.saveAll(List.of(outgoing, incoming, unrelated));

        List<Transfer> transfers = transferRepository.findBySourceAccountIdOrDestinationAccountId(100L, 100L);

        assertEquals(2, transfers.size());
        assertTrue(transfers.stream().allMatch(t ->
                t.getSourceAccountId().equals(100L) || t.getDestinationAccountId().equals(100L)));
    }

    @Test
    void shouldFindTransfersWhenAccountIsSourceInOneAndDestinationInAnother() {
        Transfer outgoing = new Transfer(
                100L,
                200L,
                new BigDecimal("500.00"),
                "Outgoing"
        );
        Transfer incoming = new Transfer(
                300L,
                100L,
                new BigDecimal("750.00"),
                "Incoming"
        );
        transferRepository.saveAll(List.of(outgoing, incoming));

        List<Transfer> transfers = transferRepository.findBySourceAccountIdOrDestinationAccountId(100L, 100L);

        assertEquals(2, transfers.size());
    }

    @Test
    void shouldFindTransfersForDistinctSourceAndDestinationAccounts() {
        Transfer transfer = new Transfer(
                10L,
                20L,
                new BigDecimal("1000.00"),
                "Payment"
        );
        transferRepository.save(transfer);

        List<Transfer> transfers = transferRepository.findBySourceAccountIdOrDestinationAccountId(10L, 20L);

        assertEquals(1, transfers.size());
    }

    @Test
    void shouldReturnEmptyWhenAccountHasNoTransfers() {
        List<Transfer> transfers = transferRepository.findBySourceAccountIdOrDestinationAccountId(999L, 999L);

        assertTrue(transfers.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenNoTransfersExist() {
        List<Transfer> transfers = transferRepository.findBySourceAccountIdOrDestinationAccountId(1L, 2L);

        assertTrue(transfers.isEmpty());
    }
}

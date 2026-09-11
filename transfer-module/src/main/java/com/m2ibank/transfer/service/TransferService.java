package com.m2ibank.transfer.service;

import com.m2ibank.account.entity.Account;
import com.m2ibank.account.service.AccountService;
import com.m2ibank.common.exception.BusinessException;
import com.m2ibank.transfer.dto.TransferRequest;
import com.m2ibank.transfer.dto.TransferResponse;
import com.m2ibank.transfer.entity.Transfer;
import com.m2ibank.transfer.repository.TransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class TransferService {

    private static final int MAX_DESCRIPTION_LENGTH = 255;

    private final TransferRepository transferRepository;
    private final AccountService accountService;

    public TransferService(TransferRepository transferRepository, AccountService accountService) {
        this.transferRepository = transferRepository;
        this.accountService = accountService;
    }

    @Transactional
    public TransferResponse createTransfer(TransferRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        // Re-checked defensively in case DTO validation was bypassed by a direct caller.
        if (request.getSourceAccountId() == null || request.getDestinationAccountId() == null) {
            throw new BusinessException("Source and destination accounts are required");
        }

        if (request.getSourceAccountId().equals(request.getDestinationAccountId())) {
            throw new BusinessException("Source and destination accounts must be different");
        }

        BigDecimal amount = request.getAmount();
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException("Transfer amount must be greater than zero");
        }

        String description = request.getDescription() == null ? "" : request.getDescription().trim();
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new BusinessException("Transfer description is too long");
        }

        Account sourceAccount = accountService.getAccountEntityById(request.getSourceAccountId());
        Account destinationAccount = accountService.getAccountEntityById(request.getDestinationAccountId());

        accountService.debitAccount(sourceAccount, amount);
        accountService.creditAccount(destinationAccount, amount);

        Transfer transfer = new Transfer(
                request.getSourceAccountId(),
                request.getDestinationAccountId(),
                amount,
                description
        );

        Transfer saved = transferRepository.save(transfer);
        return mapToResponse(saved);
    }

    public List<TransferResponse> getTransfersForAccount(Long accountId) {
        accountService.getAccountEntityById(accountId);
        return transferRepository.findBySourceAccountIdOrDestinationAccountId(accountId, accountId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private TransferResponse mapToResponse(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getSourceAccountId(),
                transfer.getDestinationAccountId(),
                transfer.getAmount(),
                transfer.getDescription(),
                transfer.getCreatedAt()
        );
    }
}

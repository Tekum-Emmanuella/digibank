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

import java.util.List;

@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final AccountService accountService;

    public TransferService(TransferRepository transferRepository, AccountService accountService) {
        this.transferRepository = transferRepository;
        this.accountService = accountService;
    }

    @Transactional
    public TransferResponse createTransfer(TransferRequest request) {
        if (request.getSourceAccountId().equals(request.getDestinationAccountId())) {
            throw new BusinessException("Source and destination accounts must be different");
        }

        Account sourceAccount = accountService.getAccountEntityById(request.getSourceAccountId());
        Account destinationAccount = accountService.getAccountEntityById(request.getDestinationAccountId());

        accountService.debitAccount(sourceAccount, request.getAmount());
        accountService.creditAccount(destinationAccount, request.getAmount());

        Transfer transfer = new Transfer(
                request.getSourceAccountId(),
                request.getDestinationAccountId(),
                request.getAmount(),
                request.getDescription()
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

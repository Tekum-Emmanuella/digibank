package com.m2ibank.transfer.controller;

import com.m2ibank.common.api.ApiResponse;
import com.m2ibank.transfer.dto.TransferRequest;
import com.m2ibank.transfer.dto.TransferResponse;
import com.m2ibank.transfer.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TransferResponse>> createTransfer(@Valid @RequestBody TransferRequest request) {
        TransferResponse response = transferService.createTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transfer executed successfully", response));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<ApiResponse<List<TransferResponse>>> getTransfersForAccount(@PathVariable Long accountId) {
        List<TransferResponse> response = transferService.getTransfersForAccount(accountId);
        return ResponseEntity.ok(ApiResponse.success("Transfers retrieved successfully", response));
    }
}

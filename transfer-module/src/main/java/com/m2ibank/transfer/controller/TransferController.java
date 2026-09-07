package com.m2ibank.transfer.controller;

import com.m2ibank.common.api.ApiResponse;
import com.m2ibank.transfer.dto.TransferRequest;
import com.m2ibank.transfer.dto.TransferResponse;
import com.m2ibank.transfer.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Transfer Management", description = "Fund transfer execution and history API")
@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @Operation(summary = "Execute a transfer", description = "Transfers funds between two distinct accounts")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Transfer executed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation or business rule failure"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Source or destination account not found")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<TransferResponse>> createTransfer(@Valid @RequestBody TransferRequest request) {
        TransferResponse response = transferService.createTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transfer executed successfully", response));
    }

    @Operation(summary = "List transfers for an account", description = "Returns transfers where the account is source or destination")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transfers retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/account/{accountId}")
    public ResponseEntity<ApiResponse<List<TransferResponse>>> getTransfersForAccount(@PathVariable Long accountId) {
        List<TransferResponse> response = transferService.getTransfersForAccount(accountId);
        return ResponseEntity.ok(ApiResponse.success("Transfers retrieved successfully", response));
    }
}

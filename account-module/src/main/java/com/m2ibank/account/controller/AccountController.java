package com.m2ibank.account.controller;

import com.m2ibank.account.dto.AccountRequest;
import com.m2ibank.account.dto.AccountResponse;
import com.m2ibank.account.service.AccountService;
import com.m2ibank.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@Valid @RequestBody AccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(@PathVariable("id") Long id) {
        AccountResponse response = accountService.getAccountById(id);
        return ResponseEntity.ok(ApiResponse.success("Account retrieved successfully", response));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccountsByCustomerId(@PathVariable("customerId") Long customerId) {
        List<AccountResponse> response = accountService.getAccountsByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success("Accounts retrieved successfully", response));
    }
}

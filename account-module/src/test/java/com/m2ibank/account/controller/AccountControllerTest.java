package com.m2ibank.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.m2ibank.account.dto.AccountRequest;
import com.m2ibank.account.dto.AccountResponse;
import com.m2ibank.account.entity.AccountType;
import com.m2ibank.account.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @Test
    void shouldReturn201WhenCreatingAccount() throws Exception {
        AccountRequest request = new AccountRequest();
        request.setCustomerId(1L);
        request.setAccountType(AccountType.CURRENT);
        request.setInitialBalance(new BigDecimal("100000.00"));

        AccountResponse response = new AccountResponse(
                1L,
                "DB-00000001",
                new BigDecimal("100000.00"),
                AccountType.CURRENT,
                1L,
                LocalDateTime.of(2026, 9, 3, 10, 0)
        );

        when(accountService.createAccount(any(AccountRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.accountNumber").value("DB-00000001"))
                .andExpect(jsonPath("$.data.customerId").value(1));

        verify(accountService).createAccount(any(AccountRequest.class));
    }

    @Test
    void shouldReturn400WhenCreatingAccountWithInvalidPayload() throws Exception {
        AccountRequest request = new AccountRequest();
        request.setCustomerId(null);
        request.setAccountType(AccountType.CURRENT);
        request.setInitialBalance(new BigDecimal("-1.00"));

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200WhenGettingAccountById() throws Exception {
        AccountResponse response = new AccountResponse(
                1L,
                "DB-00000001",
                new BigDecimal("100000.00"),
                AccountType.CURRENT,
                1L,
                LocalDateTime.of(2026, 9, 3, 10, 0)
        );

        when(accountService.getAccountById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/accounts/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.accountNumber").value("DB-00000001"));

        verify(accountService).getAccountById(1L);
    }

    @Test
    void shouldReturn200WhenGettingAccountsByCustomerId() throws Exception {
        AccountResponse response = new AccountResponse(
                1L,
                "DB-00000001",
                new BigDecimal("100000.00"),
                AccountType.CURRENT,
                1L,
                LocalDateTime.of(2026, 9, 3, 10, 0)
        );

        when(accountService.getAccountsByCustomerId(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/accounts/customer/{customerId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].customerId").value(1));

        verify(accountService).getAccountsByCustomerId(1L);
    }
}

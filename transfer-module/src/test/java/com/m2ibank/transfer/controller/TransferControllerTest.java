package com.m2ibank.transfer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.m2ibank.transfer.dto.TransferRequest;
import com.m2ibank.transfer.dto.TransferResponse;
import com.m2ibank.transfer.service.TransferService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferController.class)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransferService transferService;

    @Test
    void shouldCreateTransferAndReturnCreated() throws Exception {
        TransferResponse response = new TransferResponse(
                99L, 1L, 2L, new BigDecimal("2500.00"), "Salary transfer", LocalDateTime.now());

        when(transferService.createTransfer(any(TransferRequest.class))).thenReturn(response);

        TransferRequest request = new TransferRequest();
        request.setSourceAccountId(1L);
        request.setDestinationAccountId(2L);
        request.setAmount(new BigDecimal("2500.00"));
        request.setDescription("Salary transfer");

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Transfer executed successfully"))
                .andExpect(jsonPath("$.data.id").value(99))
                .andExpect(jsonPath("$.data.sourceAccountId").value(1))
                .andExpect(jsonPath("$.data.destinationAccountId").value(2))
                .andExpect(jsonPath("$.data.amount").value(2500.00));
    }

    @Test
    void shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setSourceAccountId(1L);
        request.setDestinationAccountId(2L);
        request.setAmount(new BigDecimal("0.00"));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetTransfersForAccount() throws Exception {
        TransferResponse response = new TransferResponse(
                5L, 10L, 20L, new BigDecimal("1000.00"), "Payment", LocalDateTime.now());

        when(transferService.getTransfersForAccount(eq(10L))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/transfers/account/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Transfers retrieved successfully"))
                .andExpect(jsonPath("$.data[0].id").value(5))
                .andExpect(jsonPath("$.data[0].sourceAccountId").value(10))
                .andExpect(jsonPath("$.data[0].destinationAccountId").value(20));
    }
}

package com.m2ibank.web.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.m2ibank.customer.dto.CustomerRequest;
import com.m2ibank.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
    }

    @Test
    void shouldCreateCustomerAndReturn201WithSuccessResponse() throws Exception {
        CustomerRequest request = new CustomerRequest();
        request.setFullName("John Doe");
        request.setEmail("john.doe@example.com");
        request.setPhoneNumber("+237600000003");
        request.setNationalId("CNI000003");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("John Doe"))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"));
    }

    @Test
    void shouldRejectDuplicateEmailAndReturn400() throws Exception {
        CustomerRequest firstRequest = new CustomerRequest();
        firstRequest.setFullName("John Doe");
        firstRequest.setEmail("duplicate@example.com");
        firstRequest.setPhoneNumber("+237600000001");
        firstRequest.setNationalId("CNI000001");

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        CustomerRequest secondRequest = new CustomerRequest();
        secondRequest.setFullName("Jane Doe");
        secondRequest.setEmail("duplicate@example.com");
        secondRequest.setPhoneNumber("+237600000002");
        secondRequest.setNationalId("CNI000002");

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldRejectDuplicatePhoneAndReturn400() throws Exception {
        CustomerRequest firstRequest = new CustomerRequest();
        firstRequest.setFullName("John Doe");
        firstRequest.setEmail("john@example.com");
        firstRequest.setPhoneNumber("+237600000001");
        firstRequest.setNationalId("CNI000001");

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        CustomerRequest secondRequest = new CustomerRequest();
        secondRequest.setFullName("Jane Doe");
        secondRequest.setEmail("jane@example.com");
        secondRequest.setPhoneNumber("+237600000001");
        secondRequest.setNationalId("CNI000002");

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnNotFoundWhenRetrievingNonExistentCustomer() throws Exception {
        mockMvc.perform(get("/api/customers/9999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }
}

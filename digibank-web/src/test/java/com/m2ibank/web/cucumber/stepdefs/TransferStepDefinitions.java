package com.m2ibank.web.cucumber.stepdefs;

import com.m2ibank.account.dto.AccountRequest;
import com.m2ibank.account.dto.AccountResponse;
import com.m2ibank.account.entity.AccountType;
import com.m2ibank.account.service.AccountService;
import com.m2ibank.customer.dto.CustomerRequest;
import com.m2ibank.customer.dto.CustomerResponse;
import com.m2ibank.customer.service.CustomerService;
import com.m2ibank.transfer.dto.TransferRequest;
import com.m2ibank.web.cucumber.TestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class TransferStepDefinitions {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TestContext testContext;

    private Long accountId;

    @Given("a customer with email {string} exists with a CURRENT account")
    public void aCustomerWithEmailExistsWithACurrentAccount(String email) {
        // Create customer
        CustomerRequest customerRequest = new CustomerRequest();
        customerRequest.setFullName("Test Customer");
        customerRequest.setEmail(email);
        customerRequest.setPhoneNumber("+237600000999");
        customerRequest.setNationalId("CNI000999");
        
        CustomerResponse customer = customerService.createCustomer(customerRequest);

        // Create account for customer
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setCustomerId(customer.getId());
        accountRequest.setAccountType(AccountType.CURRENT);
        accountRequest.setInitialBalance(new BigDecimal("100000.00"));
        
        AccountResponse account = accountService.createAccount(accountRequest);
        accountId = account.getId();
    }

    @When("I attempt to submit a transfer with the same source and destination account")
    public void iAttemptToSubmitATransferWithTheSameSourceAndDestinationAccount() {
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setSourceAccountId(accountId);
        transferRequest.setDestinationAccountId(accountId);
        transferRequest.setAmount(new BigDecimal("1000.00"));
        transferRequest.setDescription("Test self transfer");

        testContext.setResponse(restTemplate.postForEntity("/api/transfers", transferRequest, String.class));
    }

    @Then("the transfer should be rejected")
    public void theTransferShouldBeRejected() {
        assertThat(testContext.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Then("the error message should contain {string}")
    public void theErrorMessageShouldContain(String errorMessage) {
        assertThat(testContext.getResponse().getBody()).contains(errorMessage);
    }
}

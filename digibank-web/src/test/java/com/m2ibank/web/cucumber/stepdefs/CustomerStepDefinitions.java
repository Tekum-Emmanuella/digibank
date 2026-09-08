package com.m2ibank.web.cucumber.stepdefs;

import com.m2ibank.customer.dto.CustomerRequest;
import com.m2ibank.customer.entity.Customer;
import com.m2ibank.customer.repository.CustomerRepository;
import com.m2ibank.web.cucumber.TestContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerStepDefinitions {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestContext testContext;

    @Autowired
    private CustomerRepository customerRepository;

    @Given("the banking system is running")
    public void theBankingSystemIsRunning() {
        // The context is already loaded via @SpringBootTest
    }

    @Given("a customer with email {string} exists")
    public void aCustomerWithEmailExists(String email) {
        Customer customer = new Customer();
        customer.setFullName("Test Customer");
        customer.setEmail(email);
        customer.setPhoneNumber("+237600000099");
        customer.setNationalId("CNI000099");
        customerRepository.save(customer);
        testContext.setCurrentCustomer(customer);
    }

    @Given("a customer with phone number {string} exists")
    public void aCustomerWithPhoneNumberExists(String phoneNumber) {
        Customer customer = new Customer();
        customer.setFullName("Test Customer");
        customer.setEmail("test" + System.currentTimeMillis() + "@example.com");
        customer.setPhoneNumber(phoneNumber);
        customer.setNationalId("CNI" + System.currentTimeMillis());
        customerRepository.save(customer);
        testContext.setCurrentCustomer(customer);
    }

    @When("I submit a new customer request with the following details:")
    public void iSubmitANewCustomerRequestWithTheFollowingDetails(DataTable dataTable) {
        Map<String, String> customerData = dataTable.asMaps().get(0);
        
        CustomerRequest request = new CustomerRequest();
        request.setFullName(customerData.get("fullName"));
        request.setEmail(customerData.get("email"));
        request.setPhoneNumber(customerData.get("phoneNumber"));
        request.setNationalId(customerData.get("nationalId"));

        testContext.setResponse(restTemplate.postForEntity("/api/customers", request, String.class));
    }

    @Then("the customer should be created successfully")
    public void theCustomerShouldBeCreatedSuccessfully() {
        assertThat(testContext.getResponse().getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Then("the customer creation should fail")
    public void theCustomerCreationShouldFail() {
        assertThat(testContext.getResponse().getStatusCode()).isIn(
                HttpStatus.BAD_REQUEST,
                HttpStatus.CONFLICT
        );
    }

    @Then("the response should contain status code {int}")
    public void theResponseShouldContainStatusCode(int statusCode) {
        assertThat(testContext.getResponse().getStatusCode().value()).isEqualTo(statusCode);
    }

    @Then("the response should indicate success")
    public void theResponseShouldIndicateSuccess() {
        assertThat(testContext.getResponse().getBody()).contains("\"success\":true");
    }

    @Then("the error message should contain {string}")
    public void theErrorMessageShouldContain(String text) {
        String responseBody = testContext.getResponse().getBody();
        assertThat(responseBody).containsIgnoringCase(text);
    }
}

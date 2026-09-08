Feature: Transfer Management
  As a bank customer
  I want to perform transfers between accounts
  So that I can move money securely

  Scenario: Reject transfer when source and destination accounts are identical
    Given the banking system is running
    And a customer with email "alice@test.com" exists with a CURRENT account
    When I attempt to submit a transfer with the same source and destination account
    Then the transfer should be rejected
    And the response should contain status code 400
    And the error message should contain "Source and destination accounts must be different"

  Scenario: Execute a successful transfer between different accounts
    Given the banking system is running
    And a customer with email "sender@test.com" exists with a CURRENT account having balance 1000.00
    And a customer with email "receiver@test.com" exists with a CURRENT account
    When I submit a transfer of 500.00 from "sender@test.com" to "receiver@test.com"
    Then the transfer should succeed
    And the response should contain status code 201
    And the response should indicate success

  Scenario: Reject transfer when balance is insufficient
    Given the banking system is running
    And a customer with email "sender@test.com" exists with a CURRENT account having balance 100.00
    And a customer with email "receiver@test.com" exists with a CURRENT account
    When I submit a transfer of 500.00 from "sender@test.com" to "receiver@test.com"
    Then the transfer should be rejected
    And the response should contain status code 400
    And the error message should contain "insufficient balance"

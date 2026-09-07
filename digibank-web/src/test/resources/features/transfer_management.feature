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

Feature: Customer Management
  As a bank administrator
  I want to manage customer accounts
  So that I can onboard new customers to the banking system

  Scenario: Create a new customer successfully
    Given the banking system is running
    When I submit a new customer request with the following details:
      | fullName    | email                  | phoneNumber    | nationalId |
      | John Doe    | john.doe@example.com   | +237600000003  | CNI000003  |
    Then the customer should be created successfully
    And the response should contain status code 201
    And the response should indicate success

  Scenario: Reject customer with duplicate email
    Given the banking system is running
    And a customer with email "alice@test.com" exists
    When I submit a new customer request with the following details:
      | fullName    | email             | phoneNumber    | nationalId |
      | Bob Smith   | alice@test.com    | +237600000004  | CNI000004  |
    Then the customer creation should fail
    And the response should contain status code 400
    And the error message should contain "already exists"

  Scenario: Reject customer with duplicate phone number
    Given the banking system is running
    And a customer with phone number "+237600000005" exists
    When I submit a new customer request with the following details:
      | fullName    | email               | phoneNumber    | nationalId |
      | Charlie Brown | charlie@test.com  | +237600000005  | CNI000005  |
    Then the customer creation should fail
    And the response should contain status code 400
    And the error message should contain "already exists"

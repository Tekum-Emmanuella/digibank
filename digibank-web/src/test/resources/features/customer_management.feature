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

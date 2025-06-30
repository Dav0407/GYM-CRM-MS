Feature: Trainee Management

  As a Gym CRM administrator
  I want to manage trainee profiles
  So that I can register, view, update, and delete trainees.

  Scenario: Register a new trainee successfully
    Given a new trainee registration request with first name "John", last name "Doe", birth date "1990-01-15", and address "123 Main St"
    When I send a POST request to "/api/v1/trainees/register"
    Then the response status code should be 201
    And the response should contain a username and password
    And the first name in the response should be "John"
    And the last name in the response should be "Doe"

  Scenario: Get trainee profile by username
    Given a trainee "existing.trainee" exists with first name "Jane", last name "Smith"
    When I send a GET request to "/api/v1/trainees/existing.trainee"
    Then the response status code should be 200
    And the username in the response should be "existing.trainee"
    And the first name in the response should be "Jane"
    And the last name in the response should be "Smith"

  Scenario: Update an existing trainee's profile
    Given a trainee "update.trainee" exists with first name "Old", last name "Name"
    And an update trainee request for username "update.trainee" with first name "New", last name "Person", birth date "1985-05-20", address "456 Oak Ave", and active status "true"
    When I send a PUT request to "/api/v1/trainees"
    Then the response status code should be 200
    And the username in the response should be "update.trainee"
    And the first name in the response should be "New"
    And the last name in the response should be "Person"
    And the active status in the response should be "true"

  Scenario: Delete a trainee profile
    Given a trainee "delete.trainee" exists
    When I send a DELETE request to "/api/v1/trainees/delete.trainee"
    Then the response status code should be 204
    And the response body should be empty

  Scenario: Switch trainee status
    Given a trainee "status.trainee" exists with active status "true"
    When I send a PATCH request to "/api/v1/trainees/status.trainee/status"
    Then the response status code should be 200
    And the username in the response should be "status.trainee"
    And the active status in the response should be "false"
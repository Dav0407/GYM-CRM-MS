Feature: Trainee Management

  As a Gym CRM administrator
  I want to manage trainee profiles
  So that I can register, view, update, and delete trainees.

  Scenario: Register a new trainee successfully
    Given a new trainee registration request with first name "Davron", last name "Normamatov", birth date "2004-07-04", and address "123 Main St"
    When I send a POST request to "/register"
    Then the response status code should be 201
    And the response should contain a username and password
    And the first name in the response should be "Davron"
    And the last name in the response should be "Normamatov"

  Scenario: Get trainee profile by username
    Given a trainee "davron.normamatov" exists with first name "Davron", last name "Normamatov"
    When I send a GET request to "/davron.normamatov"
    Then the response status code should be 200
    And the username in the response should be "davron.normamatov"
    And the first name in the get response should be "Davron"
    And the last name in the get response should be "Normamatov"

  Scenario: Update an existing trainee's profile
    Given a trainee "davron.normamatov" exists with first name "Davron", last name "Normamatov"
    And an update trainee request for username "davron.normamatov" with first name "Debra", last name "Jessi", birth date "1985-05-20", address "456 Oak Ave", and active status "true"
    When I send a PUT request to ""
    Then the response status code should be 200
    And the username in the response should be "davron.normamatov"
    And the first name in the update response should be "Debra"
    And the last name in the update response should be "Jessi"
    And the active status in the response should be "true"


  Scenario: Switch trainee status
    Given a trainee "davron.normamatov" exists with active status "true"
    When I send a PATCH request to "/davron.normamatov/status"
    Then the response status code should be 200
    And the username in the response should be "davron.normamatov"
    And the active status in the response should be "false"

  Scenario: Delete a trainee profile
    Given a trainee "davron.normamatov" exists
    When I send a DELETE request to "/davron.normamatov"
    Then the response status code should be 204
    And the response body should be empty

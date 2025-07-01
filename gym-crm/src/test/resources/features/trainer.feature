Feature: Trainer Management

  As a Gym CRM administrator
  I want to manage trainer profiles
  So that I can register, view, update, switch status, and manage assignments for trainers.

  Scenario: Register a new trainer successfully
    Given a new trainer registration request with first name "Dilyor", last name "Sodiqov", training type "Yoga"
    When I send a POST request to trainer api "/register"
    Then the trainer response status code should be 201 for trainer api
    And the trainer response should contain a username and password
    And the first name in the trainer response should be "Dilyor"
    And the last name in the trainer response should be "Sodiqov"
    And the specialization in the trainer response should be "Yoga"
    And the trainer response should contain access and refresh tokens

  Scenario: Get trainer profile successfully
    Given a trainer exists with a username "dilyor.sodiqov" first name "Dilyor" and last name "Sodiqov"
    When I send a trainer GET request to "/dilyor.sodiqov"
    Then the trainer response status code should be 200
    And the trainer username in the response should be "dilyor.sodiqov"
    And the trainer first name in the get response should be "Dilyor"
    And the trainer last name in the get response should be "Sodiqov"
    And the trainer active status in the get response should be "true"
    And the trainer specialization in the get response should be "Yoga"

  Scenario: Update trainer profile successfully
    Given a trainer exists with a username "dilyor.sodiqov" first name "Dilyor" and last name "Sodiqov"
    And an update trainer request for username "dilyor.sodiqov" with  with first name "Debra", last name "Jessi", specialization "Cardio" and active status "true"
    When I send a PUT request to "" trainer api
    Then the trainer response status code should be 200
    And the trainer username in the response should be "dilyor.sodiqov"
    And the trainer first name in the get response should be "Debra"
    And the trainer last name in the get response should be "Jessi"
    And the trainer active status in the get response should be "true"
    And the trainer specialization in the get response should be "Cardio"

  Scenario: Get unsigned trainers for a trainee "davron.normamatov"
    Given a trainee with username "davron.normamatov" exists
    When I send a trainer GET request to "/not-assigned/davron.normamatov" for fetching available trainers
    Then the trainer response status code should be 200
    And the list of trainers should contain trainer with first name "Debra", last name with "Jessi" and with username "dilyor.sodiqov"

  Scenario: Assign a trainers list for a trainee with username "davron.normamatov"
    Given a trainee with username "davron.normamatov" exists
    And a list of trainers with usernames "emma.brown", "david.williams", "sarah.miller" to be assigned to our trainee "davron.normamatov"
    When I send a PUT request to "/assign" trainer api for assigning trainers list
    And the list of trainers should contain trainer with first name "Emma", last name with "Brown" and with username "emma.brown"
    And the list of trainers should contain trainer with first name "David", last name with "Williams" and with username "david.williams"
    And the list of trainers should contain trainer with first name "Sarah", last name with "Miller" and with username "sarah.miller"
    And the list of trainers should not contain trainer with first name "Debra", last name with "Jessi" and with username "dilyor.sodiqov"

    Scenario: Switch trainer status
      Given a trainer with username "dilyor.sodiqov" exists with active status "true"
      When I send a PATCH request to "/dilyor.sodiqov/status" for trainer api
      Then the trainer response status code should be 200 for trainer api
      And the trainer username in the response should be "dilyor.sodiqov"
      And the trainer active status in the get response should be "false"

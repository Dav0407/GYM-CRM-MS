Feature: Training Management
  As a gym CRM system user
  I want to manage training sessions
  So that I can create, retrieve, and delete trainings for trainees and trainers

  Scenario: Create a new training session successfully
    Given I authenticate to trainee account with username "john.doe" and password "password123"
    And I authenticate to trainer account with username "emma.brown" and password "password123"
    And I create a training session with the title "Professional Boxing classes" and on "2025-09-01" with a duration of 120 minutes
    When I send a POST request to "" training api
    Then the response status code should be 201 for training api
    And the training title should be "Professional Boxing classes"
    And the training duration should match 120 minutes

  Scenario: Create a new training session with a date in past
    Given I authenticate to trainee account with username "john.doe" and password "password123"
    And I authenticate to trainer account with username "emma.brown" and password "password123"
    And I create a training session with the title "Professional Boxing classes" and on "2024-09-01" with a duration of 120 minutes
    When I send a POST request to "" training api
    Then the response status code should be 400 for training api

  Scenario: Get all training sessions of a particular trainee
    Given I authenticate to trainee account with username "john.doe" and password "password123"
    And I authenticate to trainer account with username "emma.brown" and password "password123"
    When I send a POST request to "/trainees" to get trainings of a trainee
    And I get all trainings in date interval of "2000-01-01" and "2025-12-30"
    Then the response status code should be 302 for training api
    And one of the training sessions should belong to a trainer with username "emma.brown" and the training type should match the "Cardio"

  Scenario: Get all training sessions of a particular trainer
    Given I authenticate to trainee account with username "john.doe" and password "password123"
    And I authenticate to trainer account with username "emma.brown" and password "password123"
    When I send a POST request to "/trainers" to get trainings of a trainer
    And I get all trainer trainings in date interval of "2000-01-01" and "2025-12-30"
    Then the response status code should be 302 for training api
    And one of the training sessions should belong to a trainee with username "john.doe" and the training type should match the "Cardio"

  Scenario: Get All training types
    Given I authenticate to trainee account with username "john.doe" and password "password123"
    When I send a GET request to "/types" to fetch the training types list
    Then the list of training types should be returned as a response
    And type "Cardio" should be in the list
    And type "Strength Training" should be in the list
    And type "Yoga" should be in the list
    And type "Pilates" should be in the list
    And type "CrossFit" should be in the list
    And type "HIIT" should be in the list
    And type "Zumba" should be in the list
    And type "Boxing" should be in the list
    And type "Swimming" should be in the list

  Scenario: Delete a training session by its ID
    When I send a DELETE request
    Then the response status code should be 200 for training api
Feature: Trainer Management

  Scenario: Register a new trainer successfully
    Given the application is running
    And the user is unauthenticated
    When a POST request is sent to "/api/v1/trainers/register" with body:
      """
      {
        "firstName": "John",
        "lastName": "Doe",
        "trainingType": "Yoga"
      }
      """
    Then the response status code should be 201
    And the response should contain:
      | field        | value    |
      | firstName    | John     |
      | lastName     | Doe      |
      | specialization | Yoga     |
      | isActive     | true     |
    And the response should contain a "username" field
    And the response should contain a "password" field
    And the response should contain "accessToken" and "refreshToken"

  Scenario: Get trainer profile by username
    Given the application is running
    And a trainer exists with username "john.doe" and password "password123" and specialization "Yoga"
    And the current user is authenticated as "john.doe" with password "password123"
    When a GET request is sent to "/api/v1/trainers/john.doe"
    Then the response status code should be 200
    And the response should contain:
      | field          | value    |
      | firstName      | John     |
      | lastName       | Doe      |
      | username       | john.doe |
      | specialization | Yoga     |
      | isActive       | true     |
    And the response should contain an empty "trainees" list

  Scenario: Update an existing trainer's profile
    Given the application is running
    And a trainer exists with username "john.doe" and password "password123" and specialization "Yoga"
    And the current user is authenticated as "john.doe" with password "password123"
    When a PUT request is sent to "/api/v1/trainers" with body:
      """
      {
        "username": "john.doe",
        "firstName": "Jonathan",
        "lastName": "Smith",
        "trainingTypeName": "Pilates",
        "isActive": true
      }
      """
    Then the response status code should be 200
    And the response should contain:
      | field          | value      |
      | firstName      | Jonathan   |
      | lastName       | Smith      |
      | username       | john.doe   |
      | specialization | Pilates    |
      | isActive       | true       |

  Scenario: Switch trainer status (activate/deactivate)
    Given the application is running
    And a trainer exists with username "inactive.trainer" and password "pass123" and specialization "CrossFit" and active status "false"
    And the current user is authenticated as "inactive.trainer" with password "pass123"
    When a PATCH request is sent to "/api/v1/trainers/inactive.trainer/status"
    Then the response status code should be 200
    And the response should contain:
      | field          | value |
      | username       | inactive.trainer |
      | specialization | CrossFit |
      | isActive       | true  |

  Scenario: Get unassigned trainers for a trainee (assuming no trainers are assigned)
    Given the application is running
    And a trainee exists with username "some.trainee" and password "traineePass"
    And a trainer exists with username "trainer1" and password "pass1" and specialization "Weightlifting"
    And a trainer exists with username "trainer2" and password "pass2" and specialization "Cardio"
    And no trainers are assigned to "some.trainee"
    And the current user is authenticated as "some.trainee" with password "traineePass"
    When a GET request is sent to "/api/v1/trainers/not-assigned/some.trainee"
    Then the response status code should be 200
    And the response should be a list of 2 trainers
    And the list of trainers should contain trainer with username "trainer1"
    And the list of trainers should contain trainer with username "trainer2"

  # Note: The "Update trainee's trainer list" scenario is complex and requires
  #       mocking/setup of the TraineeTrainerService. It's often best to test
  #       service-level interactions with unit tests, and integration test the controller
  #       if the service is already well-tested. I'll include a basic one.
  Scenario: Update trainee's trainer list - Assign new trainer
    Given the application is running
    And a trainee exists with username "trainee.update" and password "updPass"
    And a trainer exists with username "trainer.new" and password "newPass" and specialization "Stretching"
    And "trainer.new" is not assigned to "trainee.update"
    And the current user is authenticated as "trainee.update" with password "updPass"
    When a PUT request is sent to "/api/v1/trainers/assign" with body:
      """
      {
        "traineeUsername": "trainee.update",
        "trainerUsernames": ["trainer.new"]
      }
      """
    Then the response status code should be 200
    And the response should be a list of 1 trainers
    And the list of trainers should contain trainer with username "trainer.new"
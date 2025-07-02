Feature: Trainer Working Hours Management
  As a system administrator
  I want to manage trainer working hours
  So that I can track and validate trainer workloads

  Background:
    Given the trainer working hours service is initialized

  Scenario: Calculate and save working hours for a new trainer
    Given a trainer workload request with:
      | username  | firstName | lastName | isActive | trainingDate | durationMinutes | actionType |
      | john.doe  | John      | Doe      | true     | 2025-06-15   | 120            | ADD        |
    And the trainer does not exist in the system
    When I calculate and save the working hours
    Then the trainer working hours should be saved successfully

  Scenario: Calculate and save working hours for an existing trainer
    Given a trainer workload request with:
      | username  | firstName | lastName | isActive | trainingDate | durationMinutes | actionType |
      | john.doe  | John      | Doe      | true     | 2025-06-15   | 120            | ADD        |
    And the trainer exists in the system with existing hours
    When I calculate and save the working hours
    Then the existing trainer working hours should be updated

  Scenario: Delete training hours from existing trainer
    Given a trainer workload request with:
      | username  | firstName | lastName | isActive | trainingDate | durationMinutes | actionType |
      | john.doe  | John      | Doe      | true     | 2025-06-15   | 120            | DELETE     |
    And the trainer exists in the system with existing hours
    When I calculate and save the working hours
    Then the trainer working hours should be reduced

  Scenario: Handle inactive trainer workload
    Given a trainer workload request with:
      | username  | firstName | lastName | isActive | trainingDate | durationMinutes | actionType |
      | john.doe  | John      | Doe      | false    | 2025-06-15   | 120            | ADD        |
    And the trainer exists in the system with existing hours
    When I calculate and save the working hours
    Then the trainer working hours should be reduced due to inactive status

  Scenario: Reject workload that exceeds daily limit for new trainer
    Given a trainer workload request with:
      | username  | firstName | lastName | isActive | trainingDate | durationMinutes | actionType |
      | john.doe  | John      | Doe      | true     | 2025-06-15   | 540            | ADD        |
    And the trainer does not exist in the system
    When I calculate and save the working hours
    Then an exception should be thrown with message containing "Daily limit is 8.0 hours"

  Scenario: Reject workload that exceeds daily limit for existing trainer
    Given a trainer workload request with:
      | username  | firstName | lastName | isActive | trainingDate | durationMinutes | actionType |
      | john.doe  | John      | Doe      | true     | 2025-06-15   | 300            | ADD        |
    And the trainer exists in the system with 4 hours already worked on that day
    When I calculate and save the working hours
    Then an exception should be thrown with message containing "would exceed daily limit"

  Scenario: Successfully retrieve trainer working hours
    Given the trainer "john.doe" exists with working hours data
    When I request working hours for trainer "john.doe" for year "2025" and month "JUNE"
    Then I should receive a response with:
      | trainerUsername | year | month | workingHours |
      | john.doe        | 2025 | JUNE  | 5.0          |

  Scenario: Handle request for non-existent trainer
    Given the trainer "nonexistent" does not exist in the system
    When I request working hours for trainer "nonexistent" for year "2025" and month "JUNE"
    Then an exception should be thrown with message "Trainer not found: nonexistent"

  Scenario: Handle request for non-existent month data
    Given the trainer "john.doe" exists with working hours data
    When I request working hours for trainer "john.doe" for year "2025" and month "DECEMBER"
    Then an exception should be thrown with message containing "No data found for year 2025 and month DECEMBER"

  Scenario: Successfully retrieve working hours with numeric month
    Given the trainer "john.doe" exists with numeric month data
    When I request working hours for trainer "john.doe" for year "2025" and month "6"
    Then I should receive a response with:
      | trainerUsername | year | month | workingHours |
      | john.doe        | 2025 | 6     | 5.0          |

  # Edge case scenarios
  Scenario: Handle minimum valid username length
    Given a trainer workload request with username "abc"
    And the trainer does not exist in the system
    When I calculate and save the working hours
    Then the trainer working hours should be saved successfully

  Scenario: Handle maximum valid username length
    Given a trainer workload request with username of 50 characters
    And the trainer does not exist in the system
    When I calculate and save the working hours
    Then the trainer working hours should be saved successfully

  Scenario: Handle zero training duration
    Given a trainer workload request with training duration 0 minutes
    And the trainer does not exist in the system
    When I calculate and save the working hours
    Then the trainer working hours should be saved successfully

  Scenario: Handle negative duration with delete action
    Given a trainer workload request with:
      | username  | firstName | lastName | isActive | trainingDate | durationMinutes | actionType |
      | john.doe  | John      | Doe      | true     | 2025-06-15   | -60            | DELETE     |
    And the trainer exists in the system with existing hours
    When I calculate and save the working hours
    Then the trainer working hours should be updated successfully
Feature: User Authentication and Password Management

  As a user of the Gym CRM system
  I want to be able to log in, refresh tokens, and change my password
  So that I can securely access and manage my account

  Scenario: Log in to a user successfully
    Given a user with username "sarah.miller" and password "password123"
    When I will send a POST request to an api "/login"
    Then the response status code should be 200 for user
    And the access and refresh tokens should be returned as a response

  Scenario: Log in to a non existing user
    Given a user with username "david.guetta" and password "password123"
    When I will send a POST request to an api "/login"
    Then the response status code should be 404 for user

  Scenario: Change password for a user successfully
    Given a user with username "sarah.miller" and old password "password123" and a new password "strong_password"
    When I will send a PUT request to an api "/change-password" to change password
    Then the response status code should be 200 for changing password
    And the user's password should be equal to "strong_password"

  Scenario: Change password for a non existing user
    Given a user with username "david.guetta" and old password "password123" and a new password "strong_password"
    When I will send a PUT request to an api "/change-password" to change password
    Then the response status code should be 404 for changing password

  Scenario: Logout from user account
    When I will send a GET request to a logout api "/logout"
    Then the response status code should be 200 for user
    When I will send a PUT request to an api "/change-password" to change password
    Then the response status code should be 401 for user

  Scenario: Refresh access token by using refresh token
    When I will send a PUT request to an api "/change-password" to change password
    Then the response status code should be 401 for user
    When I will send a POST request to an api "/refresh-token" to refresh my tokens
    Then the response status code should be 200 for user
package com.epam.gym_crm.cucumber.integration.steps;

import com.epam.gym_crm.dto.request.ChangePasswordRequestDTO;
import com.epam.gym_crm.dto.request.LogInRequestDTO;
import com.epam.gym_crm.dto.response.AuthenticationResponseDTO;
import com.epam.gym_crm.dto.response.UserResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PasswordEncoder encoder;

    private ResponseEntity<String> latestResponse;

    private static LogInRequestDTO logInRequest;
    private static ChangePasswordRequestDTO changePasswordRequest;
    private static String userAccessToken;
    private static String userRefreshToken;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String getUserBaseUrl() {
        return "http://localhost:" + port + "/api/v1/users";
    }

    //Scenario 1
    @Given("a user with username {string} and password {string}")
    public void aUserWithUsernameAndPassword(String username, String password) {
        logInRequest = LogInRequestDTO.builder()
                .username(username)
                .password(password)
                .build();
    }

    @When("I will send a POST request to an api {string}")
    public void iWillLogInToAUserWithUsernameAndPassword(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LogInRequestDTO> entity = new HttpEntity<>(logInRequest, headers);
        latestResponse = restTemplate.postForEntity(getUserBaseUrl() + endpoint, entity, String.class);
    }

    @Then("the response status code should be {int} for user")
    public void theResponseStatusCodeShouldBeForUserLogin(int expectedStatusCode) {
        assertEquals(expectedStatusCode, latestResponse.getStatusCode().value());
    }

    @And("the access and refresh tokens should be returned as a response")
    public void theAccessAndRefreshTokensShouldBeReturnedAsAResponse() throws JsonProcessingException {
        var response = objectMapper.readValue(latestResponse.getBody(), AuthenticationResponseDTO.class);
        userAccessToken = response.getAccessToken();
        userRefreshToken = response.getRefreshToken();
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
    }

    //Scenario 2
    @Given("a user with username {string} and old password {string} and a new password {string}")
    public void aUserWithUsernameAndOldPasswordAndNewPassword(String username, String oldPassword, String newPassword) {
        changePasswordRequest = ChangePasswordRequestDTO.builder()
                .username(username)
                .oldPassword(oldPassword)
                .newPassword(newPassword)
                .build();
    }

    @When("I will send a PUT request to an api {string} to change password")
    public void iWillSendAPutRequestToChangePassword(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(userAccessToken);
        HttpEntity<ChangePasswordRequestDTO> entity = new HttpEntity<>(changePasswordRequest, headers);
        latestResponse = restTemplate.exchange(getUserBaseUrl() + endpoint, HttpMethod.PUT, entity, String.class);
    }

    @Then("the response status code should be {int} for changing password")
    public void theResponseStatusCodeShouldBeForChangingPassword(int expectedStatusCode) {
        assertEquals(expectedStatusCode, latestResponse.getStatusCode().value());
    }

    @And("the user's password should be equal to {string}")
    public void theUserPasswordShouldBeEqualTo(String password) throws JsonProcessingException {
        var response = objectMapper.readValue(latestResponse.getBody(), UserResponseDTO.class);

        assertTrue(encoder.matches(password, response.getPassword()), "Password does not match");
    }

    //Scenario 3
    @When("I will send a GET request to a logout api {string}")
    public void iWillSendAGetRequestToLogoutApi(String endpoint) {
        String url = getUserBaseUrl() + endpoint;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userAccessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        latestResponse = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    //Scenario 4
    @When("I will send a POST request to an api {string} to refresh my tokens")
    public void iWillSendAPutRequestToRefreshMyTokens(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(userRefreshToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        latestResponse = restTemplate.postForEntity(getUserBaseUrl() + endpoint, entity, String.class);
    }

    @And("I will update my access and refresh tokens")
    public void iWillUpdateMyAccessAndRefreshTokens() throws JsonProcessingException {
        var response = objectMapper.readValue(latestResponse.getBody(), AuthenticationResponseDTO.class);
        userAccessToken = response.getAccessToken();
        userRefreshToken = response.getRefreshToken();
    }

}

package com.epam.gym_crm.cucumber.steps;

import com.epam.gym_crm.dto.request.CreateTraineeProfileRequestDTO;
import com.epam.gym_crm.dto.request.UpdateTraineeProfileRequestDTO;
import com.epam.gym_crm.dto.response.TraineeProfileResponseDTO;
import com.epam.gym_crm.dto.response.TraineeResponseDTO;
import com.epam.gym_crm.service.TraineeService;
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

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TraineeSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TraineeService traineeService; // Used for setting up initial data

    private ResponseEntity<String> latestResponse;
    private CreateTraineeProfileRequestDTO createRequest;
    private UpdateTraineeProfileRequestDTO updateRequest;
    private static String currentAccessToken;


    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1/trainees";
    }

    // Scenario 1: Register a new trainee
    @Given("a new trainee registration request with first name {string}, last name {string}, birth date {string}, and address {string}")
    public void aNewTraineeRegistrationRequest(String firstName, String lastName, String birthDateString, String address) throws Exception {
        Date birthDate = new SimpleDateFormat("yyyy-MM-dd").parse(birthDateString);
        createRequest = CreateTraineeProfileRequestDTO.builder()
                .firstName(firstName)
                .lastName(lastName)
                .dateOfBirth(birthDate)
                .address(address)
                .build();
    }

    @When("I send a POST request to {string}")
    public void iSendAPOSTRequestTo(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateTraineeProfileRequestDTO> requestEntity = new HttpEntity<>(createRequest, headers);
        latestResponse = restTemplate.postForEntity(getBaseUrl() + endpoint, requestEntity, String.class);
    }

    @Then("the response status code should be {int}")
    public void theResponseStatusCodeShouldBe(int expectedStatusCode) {
        assertEquals(expectedStatusCode, latestResponse.getStatusCode().value());
    }

    @And("the response should contain a username and password")
    public void theResponseShouldContainAUsernameAndPassword() throws Exception {
        TraineeResponseDTO responseDTO = new ObjectMapper().readValue(latestResponse.getBody(), TraineeResponseDTO.class);
        assertNotNull(responseDTO.getUsername());
        assertNotNull(responseDTO.getPassword());
    }

    @And("the first name in the response should be {string}")
    public void theFirstNameInTheResponseShouldBe(String expectedFirstName) throws Exception {
        TraineeResponseDTO responseDTO = new ObjectMapper().readValue(latestResponse.getBody(), TraineeResponseDTO.class);
        assertEquals(expectedFirstName, responseDTO.getFirstName());
    }

    @And("the last name in the response should be {string}")
    public void theLastNameInTheResponseShouldBe(String expectedLastName) throws Exception {
        TraineeResponseDTO responseDTO = new ObjectMapper().readValue(latestResponse.getBody(), TraineeResponseDTO.class);
        assertEquals(expectedLastName, responseDTO.getLastName());
        currentAccessToken = responseDTO.getAccessToken();
    }

    // Scenario 2: Get trainee profile
    @Given("a trainee {string} exists with first name {string}, last name {string}")
    public void aTraineeExistsWithName(String username, String firstName, String lastName) {
        CreateTraineeProfileRequestDTO dto = CreateTraineeProfileRequestDTO.builder()
                .firstName(firstName)
                .lastName(lastName)
                .dateOfBirth(new Date())
                .address("Address of " + username)
                .build();
        traineeService.createTraineeProfile(dto);
    }

    @When("I send a GET request to {string}")
    public void iSendAGETRequestTo(String endpoint) {
        String url = getBaseUrl() + endpoint;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentAccessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        latestResponse = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    @And("the username in the response should be {string}")
    public void theUsernameInTheResponseShouldBe(String expectedUsername) throws Exception {
        TraineeProfileResponseDTO responseDTO = new ObjectMapper().readValue(latestResponse.getBody(), TraineeProfileResponseDTO.class);
        assertEquals(expectedUsername, responseDTO.getUsername());
    }

    @And("the first name in the get response should be {string}")
    public void theFirstNameInTheGetResponseShouldBe(String expectedFirstName) throws Exception {
        TraineeProfileResponseDTO responseDTO = new ObjectMapper().readValue(latestResponse.getBody(), TraineeProfileResponseDTO.class);
        assertEquals(expectedFirstName, responseDTO.getFirstName());
    }

    @And("the last name in the get response should be {string}")
    public void theLastNameInTheGetResponseShouldBe(String expectedLastName) throws Exception {
        TraineeProfileResponseDTO responseDTO = new ObjectMapper().readValue(latestResponse.getBody(), TraineeProfileResponseDTO.class);
        assertEquals(expectedLastName, responseDTO.getLastName());
    }

    // Scenario 3: Update trainee
    @Given("an update trainee request for username {string} with first name {string}, last name {string}, birth date {string}, address {string}, and active status {string}")
    public void anUpdateRequest(String username, String firstName, String lastName, String birthDateString, String address, String isActive) throws Exception {
        Date birthDate = new SimpleDateFormat("yyyy-MM-dd").parse(birthDateString);
        updateRequest = UpdateTraineeProfileRequestDTO.builder()
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .dateOfBirth(birthDate)
                .address(address)
                .isActive(Boolean.parseBoolean(isActive))
                .build();
    }

    @When("I send a PUT request to {string}")
    public void iSendAPUTRequestTo(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(currentAccessToken);
        HttpEntity<UpdateTraineeProfileRequestDTO> entity = new HttpEntity<>(updateRequest, headers);
        latestResponse = restTemplate.exchange(getBaseUrl() + endpoint, HttpMethod.PUT, entity, String.class);
    }

    @And("the first name in the update response should be {string}")
    public void theFirstNameInTheUpdateResponseShouldBe(String expectedFirstName) throws Exception {
        TraineeProfileResponseDTO responseDTO = new ObjectMapper().readValue(latestResponse.getBody(), TraineeProfileResponseDTO.class);
        assertEquals(expectedFirstName, responseDTO.getFirstName());
    }

    @And("the last name in the update response should be {string}")
    public void theLastNameInTheUpdateResponseShouldBe(String expectedLastName) throws Exception {
        TraineeProfileResponseDTO responseDTO = new ObjectMapper().readValue(latestResponse.getBody(), TraineeProfileResponseDTO.class);
        assertEquals(expectedLastName, responseDTO.getLastName());
    }

    @And("the active status in the response should be {string}")
    public void theActiveStatusInTheResponseShouldBe(String expectedIsActive) throws Exception {
        TraineeProfileResponseDTO responseDTO = new ObjectMapper().readValue(latestResponse.getBody(), TraineeProfileResponseDTO.class);
        assertEquals(Boolean.parseBoolean(expectedIsActive), responseDTO.getIsActive());
    }

    // Scenario 4: Delete trainee
    @Given("a trainee {string} exists")
    public void aTraineeExists(String username) {
        CreateTraineeProfileRequestDTO dto = CreateTraineeProfileRequestDTO.builder()
                .firstName("Test")
                .lastName("User")
                .dateOfBirth(new Date())
                .address("Test Address of " + username)
                .build();
        traineeService.createTraineeProfile(dto);
    }

    @When("I send a DELETE request to {string}")
    public void iSendADELETERequestTo(String endpoint) {
        String url = getBaseUrl() + endpoint;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentAccessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        latestResponse = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
    }

    @And("the response body should be empty")
    public void theResponseBodyShouldBeEmpty() {
        assertNull(latestResponse.getBody());
    }

    // Scenario 5: Switch trainee status
    @Given("a trainee {string} exists with active status {string}")
    public void aTraineeExistsWithStatus(String username, String isActive) {
        CreateTraineeProfileRequestDTO dto = CreateTraineeProfileRequestDTO.builder()
                .firstName("Status")
                .lastName("Trainee")
                .dateOfBirth(new Date())
                .address("The Address of " + username)
                .build();
        TraineeResponseDTO trainee = traineeService.createTraineeProfile(dto);
        if (trainee.getIsActive() != Boolean.parseBoolean(isActive)) {
            traineeService.updateStatus(trainee.getUsername());
        }
    }

    @When("I send a PATCH request to {string}")
    public void iSendAPATCHRequestTo(String endpoint) {
        String url = getBaseUrl() + endpoint;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(currentAccessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        latestResponse = restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);
    }
}
package com.epam.gym_crm.cucumber_integration.steps;

import com.epam.gym_crm.dto.request.CreateTraineeProfileRequestDTO;
import com.epam.gym_crm.dto.request.CreateTrainerProfileRequestDTO;
import com.epam.gym_crm.dto.request.UpdateTrainerListRequestDTO;
import com.epam.gym_crm.dto.request.UpdateTrainerProfileRequestDTO;
import com.epam.gym_crm.dto.response.TraineeResponseDTO;
import com.epam.gym_crm.dto.response.TrainerProfileResponseDTO;
import com.epam.gym_crm.dto.response.TrainerResponseDTO;
import com.epam.gym_crm.dto.response.TrainerSecureResponseDTO;
import com.epam.gym_crm.exception.UserNotFoundException;
import com.epam.gym_crm.service.TraineeService;
import com.epam.gym_crm.service.TrainerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TrainerSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private TraineeService traineeService;

    private ResponseEntity<String> latestResponse;
    private CreateTrainerProfileRequestDTO createTrainerRequest;
    private UpdateTrainerProfileRequestDTO updateTrainerRequest;
    private UpdateTrainerListRequestDTO updateTrainerListRequest;

    private static String trainerAccessToken;

    private static String traineeAccessToken;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String getTrainerBaseUrl() {
        return "http://localhost:" + port + "/api/v1/trainers";
    }

    //Scenario 1
    @Given("a new trainer registration request with first name {string}, last name {string}, training type {string}")
    public void aNewTrainerRegistrationRequest(String firstName, String lastName, String trainingType) {
        createTrainerRequest = CreateTrainerProfileRequestDTO.builder()
                .firstName(firstName)
                .lastName(lastName)
                .trainingType(trainingType)
                .build();
    }

    @When("I send a POST request to trainer api {string}")
    public void iSendAPOSTRequest(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateTrainerProfileRequestDTO> requestEntity = new HttpEntity<>(createTrainerRequest, headers);
        latestResponse = restTemplate.postForEntity(getTrainerBaseUrl() + endpoint, requestEntity, String.class);
    }

    @Then("the trainer response status code should be {int} for trainer api")
    public void theTrainerResponseStatusCodeShouldBe(int expectedStatusCode) {
        assertEquals(expectedStatusCode, latestResponse.getStatusCode().value());
    }

    @And("the trainer response should contain a username and password")
    public void theTrainerResponseShouldContainAUsernameAndPassword() throws JsonProcessingException {
        TrainerResponseDTO responseDTO = objectMapper.readValue(latestResponse.getBody(), TrainerResponseDTO.class);
        assertNotNull(responseDTO.getUsername());
        assertNotNull(responseDTO.getPassword());
    }

    @And("the first name in the trainer response should be {string}")
    public void theFirstNameInTheTrainerResponseShouldBe(String expectedFirstName) throws JsonProcessingException {
        TrainerResponseDTO responseDTO = objectMapper.readValue(latestResponse.getBody(), TrainerResponseDTO.class);
        assertEquals(expectedFirstName, responseDTO.getFirstName());
    }

    @And("the last name in the trainer response should be {string}")
    public void theLastNameInTheTrainerResponseShouldBe(String expectedLastName) throws JsonProcessingException {
        TrainerResponseDTO responseDTO = objectMapper.readValue(latestResponse.getBody(), TrainerResponseDTO.class);
        assertEquals(expectedLastName, responseDTO.getLastName());
    }

    @And("the specialization in the trainer response should be {string}")
    public void theSpecializationInTheTrainerResponseShouldBe(String specialization) throws JsonProcessingException {
        TrainerResponseDTO responseDTO = objectMapper.readValue(latestResponse.getBody(), TrainerResponseDTO.class);
        assertEquals(specialization, responseDTO.getSpecialization());
    }

    @And("the trainer response should contain access and refresh tokens")
    public void theTrainerResponseShouldContainAccessAndRefreshTokens() throws JsonProcessingException {
        TrainerResponseDTO responseDTO = objectMapper.readValue(latestResponse.getBody(), TrainerResponseDTO.class);
        assertNotNull(responseDTO.getAccessToken());
        assertNotNull(responseDTO.getRefreshToken());
        trainerAccessToken = responseDTO.getAccessToken();
    }

    //Scenario 2
    @Given("a trainer exists with a username {string} first name {string} and last name {string}")
    public void aTrainerExistsWithAUsername(String trainerUsername, String firstName, String lastName) {
        try {
            trainerService.getTrainerByUsername(trainerUsername);
        } catch (UserNotFoundException exception) {
            CreateTrainerProfileRequestDTO dto = CreateTrainerProfileRequestDTO.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .trainingType(trainerUsername)
                    .build();
            trainerService.createTrainerProfile(dto);
        }
    }

    @When("I send a trainer GET request to {string}")
    public void iSendATrainerGETRequestTo(String endpoint) {
        String url = getTrainerBaseUrl() + endpoint;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(trainerAccessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        latestResponse = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    @Then("the trainer response status code should be {int}")
    public void theTrainerGetResponseStatusCodeShouldBe(int expectedStatusCode) {
        assertEquals(expectedStatusCode, latestResponse.getStatusCode().value());
    }

    @And("the trainer username in the response should be {string}")
    public void theTrainerUsernameInTheResponseShouldBe(String expectedUsername) throws JsonProcessingException {
        TrainerProfileResponseDTO responseDTO = objectMapper.readValue(latestResponse.getBody(), TrainerProfileResponseDTO.class);
        assertEquals(expectedUsername, responseDTO.getUsername());
    }

    @And("the trainer first name in the get response should be {string}")
    public void theTrainerFirstNameInTheResponseShouldBe(String expectedFirstName) throws JsonProcessingException {
        TrainerProfileResponseDTO responseDTO = objectMapper.readValue(latestResponse.getBody(), TrainerProfileResponseDTO.class);
        assertEquals(expectedFirstName, responseDTO.getFirstName());
    }

    @And("the trainer last name in the get response should be {string}")
    public void theTrainerLastNameInTheResponseShouldBe(String expectedLastName) throws JsonProcessingException {
        TrainerProfileResponseDTO responseDTO = objectMapper.readValue(latestResponse.getBody(), TrainerProfileResponseDTO.class);
        assertEquals(expectedLastName, responseDTO.getLastName());
    }

    @And("the trainer active status in the get response should be {string}")
    public void theTrainerActiveStatusInTheResponseShouldBe(String expectedActiveStatus) throws JsonProcessingException {
        TrainerProfileResponseDTO responseDTO = objectMapper.readValue(latestResponse.getBody(), TrainerProfileResponseDTO.class);
        assertEquals(Boolean.parseBoolean(expectedActiveStatus), responseDTO.getIsActive());
    }

    @And("the trainer specialization in the get response should be {string}")
    public void theSpecializationInTheResponseShouldBe(String expectedSpecialization) throws JsonProcessingException {
        TrainerProfileResponseDTO responseDTO = objectMapper.readValue(latestResponse.getBody(), TrainerProfileResponseDTO.class);
        assertEquals(expectedSpecialization, responseDTO.getSpecialization());
    }

    //Scenario 3
    @And("an update trainer request for username {string} with  with first name {string}, last name {string}, specialization {string} and active status {string}")
    public void updateTrainerRequest(String username, String firstName, String lastName, String specialization, String activeStatus) {
        updateTrainerRequest = UpdateTrainerProfileRequestDTO.builder()
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .isActive(Boolean.parseBoolean(activeStatus))
                .trainingTypeName(specialization)
                .build();
    }

    @When("I send a PUT request to {string} trainer api")
    public void iSendAPUTRequestTo(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(trainerAccessToken);
        HttpEntity<UpdateTrainerProfileRequestDTO> entity = new HttpEntity<>(updateTrainerRequest, headers);
        latestResponse = restTemplate.exchange(getTrainerBaseUrl() + endpoint, HttpMethod.PUT, entity, String.class);
    }

    //Scenario 4
    @Transactional
    @Given("a trainee with username {string} exists")
    public void aTraineeWithUsernameExists(String username) throws ParseException {
        try {
            traineeService.getTraineeByUsername(username);
        } catch (UserNotFoundException exception) {
            Date birthDate = new SimpleDateFormat("yyyy-MM-dd").parse("2004-07-04");
            CreateTraineeProfileRequestDTO dto = CreateTraineeProfileRequestDTO.builder()
                    .firstName("Davron")
                    .lastName("Normamatov")
                    .dateOfBirth(birthDate)
                    .address("123 Main St")
                    .build();
            TraineeResponseDTO traineeProfile = traineeService.createTraineeProfile(dto);
            traineeAccessToken = traineeProfile.getAccessToken();
        }
    }

    @When("I send a trainer GET request to {string} for fetching available trainers")
    public void iSendATrainerGETRequestToForFetchingAvailableTrainers(String endpoint) {
        String url = getTrainerBaseUrl() + endpoint;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(traineeAccessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        latestResponse = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    @And("the list of trainers should contain trainer with first name {string}, last name with {string} and with username {string}")
    public void aListShouldContainATrainer(String firstName, String lastName, String username) throws JsonProcessingException {
        List<TrainerSecureResponseDTO> trainers = objectMapper.readValue(latestResponse.getBody(), new TypeReference<>() {
        });
        boolean found = trainers.stream()
                .anyMatch(t ->
                        t.getFirstName().trim().equals(firstName.trim()) &&
                                t.getLastName().trim().equals(lastName.trim()) && t.getUsername().trim().equals(username.trim()));

        assertTrue(found, String.format(
                "Expected trainer with first name '%s', last name '%s', username '%s' not found in: %s",
                firstName, lastName, username, trainers
        ));
    }

    //Scenario 5
    @And("a list of trainers with usernames {string}, {string}, {string} to be assigned to our trainee {string}")
    public void assignListOfTrainersToATrainee(String trainerUsername1, String trainerUsername2, String trainerUsername3, String traineeUsername) {

        updateTrainerListRequest = UpdateTrainerListRequestDTO.builder()
                .traineeUsername(traineeUsername)
                .trainerUsernames(List.of(trainerUsername1, trainerUsername2, trainerUsername3))
                .build();
    }

    @When("I send a PUT request to {string} trainer api for assigning trainers list")
    public void iSendATrainerPUTRequestForAssigningTrainersList(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(traineeAccessToken);
        HttpEntity<UpdateTrainerListRequestDTO> entity = new HttpEntity<>(updateTrainerListRequest, headers);
        latestResponse = restTemplate.exchange(getTrainerBaseUrl() + endpoint, HttpMethod.PUT, entity, String.class);
    }

    @And("the list of trainers should not contain trainer with first name {string}, last name with {string} and with username {string}")
    public void aListShouldNotContainTrainer(String firstName, String lastName, String username) throws JsonProcessingException {
        List<TrainerSecureResponseDTO> trainers = objectMapper.readValue(latestResponse.getBody(), new TypeReference<>() {
        });

        boolean found = trainers.stream()
                .anyMatch(t ->
                        t.getFirstName().trim().equals(firstName.trim()) &&
                                t.getLastName().trim().equals(lastName.trim()) && t.getUsername().trim().equals(username.trim()));


        assertFalse(found, String.format(
                "Expected trainer with first name '%s', last name '%s', username '%s' found in: %s",
                firstName, lastName, username, trainers
        ));
    }

    //Scenario 6
    @Given("a trainer with username {string} exists with active status {string}")
    public void aTrainerWithActiveStatusExists(String username, String status) {
        try {
            TrainerProfileResponseDTO trainerByUsername = trainerService.getTrainerByUsername(username);
            if (trainerByUsername.getIsActive() != Boolean.parseBoolean(status)) fail();
        } catch (UserNotFoundException exception) {
            CreateTrainerProfileRequestDTO dto = CreateTrainerProfileRequestDTO.builder()
                    .firstName("Dilyor")
                    .lastName("Sodiqov")
                    .trainingType("Cardio")
                    .build();
            trainerService.createTrainerProfile(dto);
        }
    }

    @When("I send a PATCH request to {string} for trainer api")
    public void iSendAPatchRequestForTrainerApi(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(trainerAccessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        latestResponse = restTemplate.exchange(getTrainerBaseUrl() + endpoint, HttpMethod.PATCH, entity, String.class);
    }
}
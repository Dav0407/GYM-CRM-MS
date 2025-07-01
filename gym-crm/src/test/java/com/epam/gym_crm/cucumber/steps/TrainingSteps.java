package com.epam.gym_crm.cucumber.steps;

import com.epam.gym_crm.dto.request.AddTrainingRequestDTO;
import com.epam.gym_crm.dto.request.LogInRequestDTO;
import com.epam.gym_crm.dto.response.TraineeTrainingResponseDTO;
import com.epam.gym_crm.dto.response.TrainerTrainingResponseDTO;
import com.epam.gym_crm.dto.response.TrainingResponseDTO;
import com.epam.gym_crm.dto.response.TrainingTypeResponseDTO;
import com.epam.gym_crm.service.TrainingService;
import com.epam.gym_crm.service.UserService;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TrainingSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserService userService;

    private ResponseEntity<String> latestResponse;
    private static AddTrainingRequestDTO addTrainingRequest;

    private static String trainerAccessToken;
    private static String traineeAccessToken;
    private static String traineeUsername;
    private static String trainerUsername;
    private static Long trainingId;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String getTrainingBaseUrl() {
        return "http://localhost:" + port + "/api/v1/trainings";
    }

    //Scenario 1
    @Given("I authenticate to trainee account with username {string} and password {string}")
    public void iAuthenticateToTraineeAccountWithUsernameAndPassword(String username, String password) {
        traineeUsername = username;
        var request = LogInRequestDTO.builder()
                .username(username)
                .password(password)
                .build();
        var login = userService.login(request);
        traineeAccessToken = login.getAccessToken();
    }

    @And("I authenticate to trainer account with username {string} and password {string}")
    public void iAuthenticateToTrainerAccountWithUsernameAndPassword(String username, String password) {
        trainerUsername = username;
        var request = LogInRequestDTO.builder()
                .username(username)
                .password(password)
                .build();
        var login = userService.login(request);
        trainerAccessToken = login.getAccessToken();
    }

    @And("I create a training session with the title {string} and on {string} with a duration of {int} minutes")
    public void createTrainingSession(String title, String trainingDate, int minutes) throws ParseException {
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(trainingDate);
        addTrainingRequest = AddTrainingRequestDTO.builder()
                .traineeUsername(traineeUsername)
                .trainerUsername(trainerUsername)
                .trainingName(title)
                .trainingDate(date)
                .trainingDurationInMinutes(minutes)
                .build();
    }

    @When("I send a POST request to {string} training api")
    public void iSendAPOSTRequestToTrainingApi(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(trainerAccessToken);
        HttpEntity<AddTrainingRequestDTO> entity = new HttpEntity<>(addTrainingRequest, headers);
        latestResponse = restTemplate.postForEntity(getTrainingBaseUrl() + endpoint, entity, String.class);
    }

    @Then("the response status code should be {int} for training api")
    public void theResponseStatusCodeShouldBeForTrainingApi(int statusCode) {
        assertEquals(statusCode, latestResponse.getStatusCode().value());
    }

    @And("the training title should be {string}")
    public void theTrainingTitleShouldBe(String title) throws JsonProcessingException {
        var response = objectMapper.readValue(latestResponse.getBody(), TrainingResponseDTO.class);
        assertEquals(title, response.getTrainingName());
        trainingId = response.getId();
    }

    @And("the training duration should match {int} minutes")
    public void theTrainingDurationShouldMatchMinutes(int minutes) throws JsonProcessingException {
        var response = objectMapper.readValue(latestResponse.getBody(), TrainingResponseDTO.class);
        assertEquals(minutes, response.getTrainingDuration());
    }

    //Scenario 2
    @When("I send a POST request to {string} to get trainings of a trainee")
    public void iSendAPOSTRequestToGetTrainingsOfATrainee(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(traineeAccessToken);
        HttpEntity<AddTrainingRequestDTO> entity = new HttpEntity<>(addTrainingRequest, headers);
        latestResponse = restTemplate.postForEntity(getTrainingBaseUrl() + endpoint, entity, String.class);
    }

    @And("I get all trainings in date interval of {string} and {string}")
    public void iGetAllTrainingsInDateIntervalOf(String fromDate, String toDate) throws JsonProcessingException {
        LocalDate from = LocalDate.parse(fromDate);
        LocalDate to = LocalDate.parse(toDate);

        List<TraineeTrainingResponseDTO> response = objectMapper.readValue(latestResponse.getBody(), new TypeReference<>() {
        });

        for (TraineeTrainingResponseDTO training : response) {
            Date date = training.getTrainingDate();
            LocalDate trainingDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            assertFalse(trainingDate.isBefore(from), "Training date is before 'from' date");
            assertFalse(trainingDate.isAfter(to), "Training date is after 'to' date");
        }
    }

    @And("one of the training sessions should belong to a trainer with username {string} and the training type should match the {string}")
    public void theTrainingTypeShouldMatchThe(String trainerUsername, String trainingType) throws JsonProcessingException {
        List<TraineeTrainingResponseDTO> response = objectMapper.readValue(latestResponse.getBody(), new TypeReference<>() {
        });

        boolean found = response.stream()
                .anyMatch(training ->
                        trainerUsername.equals(training.getTrainerName()) &&
                                trainingType.equalsIgnoreCase(training.getTrainingType())
                );

        assertTrue(found, String.format("No training found with trainer '%s' and type '%s'", trainerUsername, trainingType));
    }

    //Scenario 3
    @When("I send a POST request to {string} to get trainings of a trainer")
    public void iSendAPOSTRequestToGetTrainingsOfATrainer(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(trainerAccessToken);
        HttpEntity<AddTrainingRequestDTO> entity = new HttpEntity<>(addTrainingRequest, headers);
        latestResponse = restTemplate.postForEntity(getTrainingBaseUrl() + endpoint, entity, String.class);
    }

    @And("I get all trainer trainings in date interval of {string} and {string}")
    public void iGetAllTrainerTrainingsInDateIntervalOf(String fromDate, String toDate) throws JsonProcessingException {
        LocalDate from = LocalDate.parse(fromDate);
        LocalDate to = LocalDate.parse(toDate);

        List<TrainerTrainingResponseDTO> response = objectMapper.readValue(latestResponse.getBody(), new TypeReference<>() {
        });

        for (TrainerTrainingResponseDTO training : response) {
            Date date = training.getTrainingDate();
            LocalDate trainingDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            assertFalse(trainingDate.isBefore(from), "Training date is before 'from' date");
            assertFalse(trainingDate.isAfter(to), "Training date is after 'to' date");
        }
    }

    @And("one of the training sessions should belong to a trainee with username {string} and the training type should match the {string}")
    public void theTrainingTypeShouldMatchTheForTrainer(String traineeName, String trainingType) throws JsonProcessingException {
        List<TrainerTrainingResponseDTO> response = objectMapper.readValue(latestResponse.getBody(), new TypeReference<>() {
        });

        boolean found = response.stream()
                .anyMatch(training ->
                        traineeName.equals(training.getTraineeName()) &&
                                trainingType.equalsIgnoreCase(training.getTrainingType())
                );

        assertTrue(found, String.format("No training found with trainee '%s' and type '%s'", traineeName, trainingType));
    }

    //Scenario 4
    @When("I send a GET request to {string} to fetch the training types list")
    public void iSendAPOSTRequestToFetchTheTrainingTypesList(String endpoint) {
        String url = getTrainingBaseUrl() + endpoint;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(traineeAccessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        latestResponse = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    @Then("the list of training types should be returned as a response")
    public void theListOfTrainingTypesShouldBeReturnedAsAResponse() throws JsonProcessingException {
        List<TrainingTypeResponseDTO> response = objectMapper.readValue(latestResponse.getBody(), new TypeReference<>() {
        });
        assertNotNull(response);
    }

    @And("type {string} should be in the list")
    public void typeShouldBeInTheList(String type) throws JsonProcessingException {
        List<TrainingTypeResponseDTO> response = objectMapper.readValue(latestResponse.getBody(), new TypeReference<>() {});

        boolean found = response.stream()
                .anyMatch(trainingType -> trainingType.getTrainingTypeName().equalsIgnoreCase(type));

        assertTrue(found, "Expected training type '" + type + "' to be present in the response, but it was not found.");
    }

    //Scenario 5
    @When("I send a DELETE request")
    public void iSendADELETERequest() {
        String url = getTrainingBaseUrl() + "/" + trainingId;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(trainerAccessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        latestResponse = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
    }

}

package com.epam.gym_crm.cucumber.steps;

import com.epam.gym_crm.GymcrmApplication;
import com.epam.gym_crm.entity.Trainer;
import com.epam.gym_crm.entity.TrainingType;
import com.epam.gym_crm.entity.User;
import com.epam.gym_crm.repository.TraineeRepository;
import com.epam.gym_crm.repository.TrainerRepository;
import com.epam.gym_crm.repository.TrainingTypeRepository;
import com.epam.gym_crm.repository.UserRepository;
import com.epam.gym_crm.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = GymcrmApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TrainerSteps {

    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;
    private ResultActions resultActions;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TrainerRepository trainerRepository;
    @Autowired
    private TraineeRepository traineeRepository; // Needed for trainee setup
    @Autowired
    private TrainingTypeRepository trainingTypeRepository; // Needed for specialization
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    // A shared context for test data between steps
    private TestContext testContext = TestContext.CONTEXT; // Reusing the same singleton context

    @Given("the application is running")
    public void theApplicationIsRunning() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        testContext.reset(); // Clear context for new scenario
        userRepository.deleteAll(); // Clean up users to ensure fresh state
        trainerRepository.deleteAll(); // Clean up trainers
        traineeRepository.deleteAll(); // Clean up trainees
        trainingTypeRepository.deleteAll(); // Clean up training types
    }

    @And("the user is unauthenticated")
    public void theUserIsUnauthenticated() {
        // No authentication headers will be added
        testContext.reset();
    }

    @When("a POST request is sent to {string} with body:")
    public void aPOSTRequestIsSentToWithBody(String endpoint, String body) throws Exception {
        resultActions = mockMvc.perform(post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @When("a GET request is sent to {string}")
    public void aGETRequestIsSentTo(String endpoint) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        if (testContext.getAuthToken() != null) {
            headers.setBearerAuth(testContext.getAuthToken());
        }
        resultActions = mockMvc.perform(get(endpoint)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON));
    }

    @When("a PUT request is sent to {string} with body:")
    public void aPUTRequestIsSentToWithBody(String endpoint, String body) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        if (testContext.getAuthToken() != null) {
            headers.setBearerAuth(testContext.getAuthToken());
        }
        resultActions = mockMvc.perform(put(endpoint)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @When("a PATCH request is sent to {string}")
    public void aPATCHRequestIsSentTo(String endpoint) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        if (testContext.getAuthToken() != null) {
            headers.setBearerAuth(testContext.getAuthToken());
        }
        resultActions = mockMvc.perform(patch(endpoint)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON));
    }

    @Then("the response status code should be {int}")
    public void theResponseStatusCodeShouldBe(int statusCode) throws Exception {
        resultActions.andExpect(status().is(statusCode));
    }

    @And("the response should contain:")
    public void theResponseShouldContain(io.cucumber.datatable.DataTable dataTable) throws Exception {
        List<Map<String, String>> expectedFields = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : expectedFields) {
            String field = row.get("field");
            String value = row.get("value");
            resultActions.andExpect(jsonPath("$." + field).value(value));
        }
    }

    @And("the response should contain a {string} field")
    public void theResponseShouldContainAField(String fieldName) throws Exception {
        resultActions.andExpect(jsonPath("$." + fieldName).exists());
    }

    @And("the response should contain {string} and {string}")
    public void theResponseShouldContainAnd(String field1, String field2) throws Exception {
        resultActions.andExpect(jsonPath("$." + field1).exists());
        resultActions.andExpect(jsonPath("$." + field2).exists());
    }

    @Given("a trainer exists with username {string} and password {string} and specialization {string}")
    public void aTrainerExistsWithUsernameAndPasswordAndSpecialization(String username, String password, String specializationName) {
        TrainingType trainingType = trainingTypeRepository.findByTrainingTypeNameIgnoreCase(specializationName)
                .orElseGet(() -> trainingTypeRepository.save(TrainingType.builder().trainingTypeName(specializationName).build()));

        User user = User.builder()
                .firstName(username.split("\\.")[0])
                .lastName(username.split("\\.")[1])
                .username(username)
                .password(passwordEncoder.encode(password)) // Encode the password
                .isActive(true)
                .role(User.Role.TRAINER)
                .build();
        userRepository.save(user);

        Trainer trainer = Trainer.builder()
                .user(user)
                .specialization(trainingType)
                .build();
        trainerRepository.save(trainer);

        testContext.addUserData(username, password, user); // Store password for later auth if needed
    }

    @Given("a trainer exists with username {string} and password {string} and specialization {string} and active status {string}")
    public void aTrainerExistsWithUsernameAndPasswordAndSpecializationAndActiveStatus(String username, String password, String specializationName, String isActive) {
        TrainingType trainingType = trainingTypeRepository.findByTrainingTypeNameIgnoreCase(specializationName)
                .orElseGet(() -> trainingTypeRepository.save(TrainingType.builder().trainingTypeName(specializationName).build()));

        User user = User.builder()
                .firstName(username.split("\\.")[0])
                .lastName(username.split("\\.")[1])
                .username(username)
                .password(passwordEncoder.encode(password)) // Encode the password
                .isActive(Boolean.parseBoolean(isActive))
                .role(User.Role.TRAINER)
                .build();
        userRepository.save(user);

        Trainer trainer = Trainer.builder()
                .user(user)
                .specialization(trainingType)
                .build();
        trainerRepository.save(trainer);

        testContext.addUserData(username, password, user);
    }

    @And("the current user is authenticated as {string} with password {string}")
    public void theCurrentUserIsAuthenticatedAsWithPassword(String username, String password) {
        // Authenticate the user and set the token in the context
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found: " + username));
        String token = jwtService.generateAccessToken(user);
        testContext.setAuthToken(token);
    }

    @And("the response should contain an empty {string} list")
    public void theResponseShouldContainAnEmptyList(String listName) throws Exception {
        resultActions.andExpect(jsonPath("$." + listName).isArray());
        resultActions.andExpect(jsonPath("$." + listName).isEmpty());
    }

    @Given("a trainee exists with username {string} and password {string}")
    public void aTraineeExistsWithUsernameAndPassword(String username, String password) {
        User user = User.builder()
                .firstName(username.split("\\.")[0])
                .lastName(username.split("\\.")[1])
                .username(username)
                .password(passwordEncoder.encode(password))
                .isActive(true)
                .role(User.Role.TRAINEE)
                .build();
        userRepository.save(user);

        com.epam.gym_crm.entity.Trainee trainee = com.epam.gym_crm.entity.Trainee.builder()
                .user(user)
                .build();
        traineeRepository.save(trainee);

        testContext.addUserData(username, password, user);
    }

    @And("no trainers are assigned to {string}")
    public void noTrainersAreAssignedTo(String traineeUsername) {
        // This implicitly relies on the clean-up in "the application is running" step
        // For a more robust test, you might explicitly detach trainers if they were pre-existing.
        // For H2, clean-up usually suffices.
    }

    @And("the response should be a list of {int} trainers")
    public void theResponseShouldBeAListOfTrainers(int expectedSize) throws Exception {
        resultActions.andExpect(jsonPath("$").isArray());
        resultActions.andExpect(jsonPath("$.length()").value(expectedSize));
    }

    @And("the list of trainers should contain trainer with username {string}")
    public void theListOfTrainersShouldContainTrainerWithUsername(String username) throws Exception {
        resultActions.andExpect(jsonPath("$[*].username").value(
                org.hamcrest.Matchers.hasItem(username)
        ));
    }

    @And("the list of trainers should not contain trainer with username {string}")
    public void theListOfTrainersShouldNotContainTrainerWithUsername(String username) throws Exception {
        resultActions.andExpect(jsonPath("$[*].username").value(
                org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem(username))
        ));
    }

    @And("{string} is not assigned to {string}")
    public void isNotAssignedTo(String trainerUsername, String traineeUsername) {
        // This step is mostly for clarity in the feature file.
        // The data setup in the "Given" steps and the cleanup in "the application is running"
        // ensures no assignments exist unless explicitly made.
    }
}
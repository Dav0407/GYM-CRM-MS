package com.epam.trainer_session_management.cucumber;

import com.epam.trainer_session_management.document.TrainerWorkingHours;
import com.epam.trainer_session_management.dto.TrainerWorkloadRequest;
import com.epam.trainer_session_management.dto.TrainerWorkloadResponse;
import com.epam.trainer_session_management.enums.ActionType;
import com.epam.trainer_session_management.repository.TrainerWorkingHoursRepository;
import com.epam.trainer_session_management.service.impl.TrainerWorkingHoursServiceImpl;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@CucumberContextConfiguration
@SpringBootTest(
        classes = {TrainerWorkingHoursServiceImpl.class},
        properties = { // This is the correct way to exclude auto-configurations
                "spring.autoconfigure.exclude=" +
                        "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration"
        }
)
public class TrainerWorkingHoursSteps {

    @MockitoBean // Ensure this import is: org.springframework.boot.test.mock.mockito.MockBean
    private TrainerWorkingHoursRepository repository;

    @Autowired
    private TrainerWorkingHoursServiceImpl service;

    private TrainerWorkloadRequest request;
    private TrainerWorkloadResponse response;
    private Exception thrownException;
    private TrainerWorkingHours existingTrainer;

    @Given("the trainer working hours service is initialized")
    public void theTrainerWorkingHoursServiceIsInitialized() {
        // Reset the mock's behavior and the test state variables for each scenario
        reset(repository);
        thrownException = null;
        request = null;
        response = null;
        existingTrainer = null; // Also reset existingTrainer to avoid state leakage
    }

    @Given("a trainer workload request with:")
    public void aTrainerWorkloadRequestWith(DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().get(0);

        Date trainingDate = Date.from(LocalDate.parse(data.get("trainingDate"))
                .atStartOfDay(ZoneId.systemDefault()).toInstant());

        request = TrainerWorkloadRequest.builder()
                .trainerUsername(data.get("username"))
                .trainerFirstName(data.get("firstName"))
                .trainerLastName(data.get("lastName"))
                .isActive(Boolean.parseBoolean(data.get("isActive")))
                .trainingDate(trainingDate)
                .trainingDurationInMinutes(Integer.parseInt(data.get("durationMinutes")))
                .actionType(ActionType.valueOf(data.get("actionType")))
                .build();
    }

    @Given("a trainer workload request with invalid {string}: {string}")
    public void aTrainerWorkloadRequestWithInvalid(String field, String value) {
        // Only call createBaseRequest if the 'request' itself isn't being set to null
        if (!"request".equals(field)) {
            createBaseRequest();
        }

        switch (field) {
            case "request":
                if ("null".equals(value)) {
                    request = null;
                }
                break;
            case "username":
                setUsernameValue(value);
                break;
            case "firstName":
                setFirstNameValue(value);
                break;
            case "lastName":
                setLastNameValue(value);
                break;
            case "isActive":
                if ("null".equals(value)) {
                    request.setIsActive(null);
                }
                break;
            case "trainingDate":
                if ("null".equals(value)) {
                    request.setTrainingDate(null);
                }
                break;
            case "trainingDurationInMinutes":
                if ("null".equals(value)) {
                    request.setTrainingDurationInMinutes(null);
                }
                break;
            case "actionType":
                if ("null".equals(value)) {
                    request.setActionType(null);
                }
                break;
            default:
                // Optionally throw an exception or log a warning for unhandled fields
                System.err.println("Warning: Unhandled invalid field for TrainerWorkloadRequest: " + field);
                break;
        }
    }

    @Given("a trainer workload request with username {string}")
    public void aTrainerWorkloadRequestWithUsername(String username) {
        createBaseRequest();
        request.setTrainerUsername(username);
    }

    @Given("a trainer workload request with username of {int} characters")
    public void aTrainerWorkloadRequestWithUsernameOfCharacters(int length) {
        createBaseRequest();
        request.setTrainerUsername("a".repeat(length));
    }

    @Given("a trainer workload request with training duration {int} minutes")
    public void aTrainerWorkloadRequestWithTrainingDuration(int duration) {
        createBaseRequest();
        request.setTrainingDurationInMinutes(duration);
    }

    @Given("the trainer does not exist in the system")
    public void theTrainerDoesNotExistInTheSystem() {
        when(repository.findById(anyString())).thenReturn(Optional.empty());
    }

    @Given("the trainer exists in the system with existing hours")
    public void theTrainerExistsInTheSystemWithExistingHours() {
        existingTrainer = createExistingTrainer();
        when(repository.findById("john.doe")).thenReturn(Optional.of(existingTrainer));
    }

    @Given("the trainer exists in the system with {int} hours already worked on that day")
    public void theTrainerExistsInTheSystemWithHoursAlreadyWorked(int hours) {
        existingTrainer = createTrainerWithExistingHours(hours);
        when(repository.findById("john.doe")).thenReturn(Optional.of(existingTrainer));
    }

    @Given("the trainer {string} exists with working hours data")
    public void theTrainerExistsWithWorkingHoursData(String username) {
        existingTrainer = createExistingTrainer();
        when(repository.findById(username)).thenReturn(Optional.of(existingTrainer));
    }

    @Given("the trainer {string} does not exist in the system")
    public void theTrainerDoesNotExistInTheSystem(String username) {
        when(repository.findById(username)).thenReturn(Optional.empty());
    }

    @Given("the trainer {string} exists with numeric month data")
    public void theTrainerExistsWithNumericMonthData(String username) {
        existingTrainer = createTrainerWithNumericMonth();
        when(repository.findById(username)).thenReturn(Optional.of(existingTrainer));
    }

    @When("I calculate and save the working hours")
    public void iCalculateAndSaveTheWorkingHours() {
        try {
            service.calculateAndSave(request);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @When("I request working hours for trainer {string} for year {string} and month {string}")
    public void iRequestWorkingHoursForTrainer(String username, String year, String month) {
        try {
            response = service.getTrainerWorkingHours(username, year, month);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @When("I request working hours with invalid {string}: {string}")
    public void iRequestWorkingHoursWithInvalid(String field, String value) {
        try {
            String username = "john.doe";
            String year = "2025";
            String month = "JUNE";

            switch (field) {
                case "username":
                    username = getInvalidValue(value);
                    break;
                case "year":
                    year = getInvalidYearValue(value);
                    break;
                case "month":
                    month = getInvalidMonthValue(value);
                    break;
                default:
                    // Optionally throw an exception or log a warning for unhandled fields
                    System.err.println("Warning: Unhandled invalid field for getTrainerWorkingHours: " + field);
                    break;
            }

            response = service.getTrainerWorkingHours(username, year, month);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("the trainer working hours should be saved successfully")
    public void theTrainerWorkingHoursShouldBeSavedSuccessfully() {
        assertNull(thrownException);
        verify(repository).save(any(TrainerWorkingHours.class));
    }

    @Then("the existing trainer working hours should be updated")
    public void theExistingTrainerWorkingHoursShouldBeUpdated() {
        assertNull(thrownException);
        verify(repository).save(existingTrainer);
    }

    @Then("the trainer working hours should be reduced")
    public void theTrainerWorkingHoursShouldBeReduced() {
        assertNull(thrownException);
        verify(repository).save(existingTrainer);
    }

    @Then("the trainer working hours should be reduced due to inactive status")
    public void theTrainerWorkingHoursShouldBeReducedDueToInactiveStatus() {
        assertNull(thrownException);
        verify(repository).save(existingTrainer);
    }

    @Then("the trainer working hours should be updated successfully")
    public void theTrainerWorkingHoursShouldBeUpdatedSuccessfully() {
        assertNull(thrownException);
        verify(repository).save(existingTrainer);
    }

    @Then("an exception should be thrown with message containing {string}")
    public void anExceptionShouldBeThrownWithMessageContaining(String expectedMessage) {
        assertNotNull(thrownException);
        assertTrue(thrownException.getMessage().contains(expectedMessage));
        verify(repository, never()).save(any());
    }

    @Then("an exception should be thrown with message {string}")
    public void anExceptionShouldBeThrownWithMessage(String expectedMessage) {
        assertNotNull(thrownException);
        assertEquals(expectedMessage, thrownException.getMessage());
    }

    @Then("I should receive a response with:")
    public void iShouldReceiveAResponseWith(DataTable dataTable) {
        assertNull(thrownException);
        assertNotNull(response);
        Map<String, String> expectedData = dataTable.asMaps().get(0);
        assertEquals(expectedData.get("trainerUsername"), response.getTrainerUsername());
        assertEquals(expectedData.get("year"), response.getYear());
        assertEquals(expectedData.get("month"), response.getMonth());
        assertEquals(Float.parseFloat(expectedData.get("workingHours")), response.getWorkingHours());
    }

    // Helper methods
    private void createBaseRequest() {
        Date testDate = Date.from(LocalDate.of(2025, 6, 15).atStartOfDay(ZoneId.systemDefault()).toInstant());

        request = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(testDate)
                .trainingDurationInMinutes(120)
                .actionType(ActionType.ADD)
                .build();
    }

    private void setUsernameValue(String value) {
        switch (value) {
            case "null":
                request.setTrainerUsername(null);
                break;
            case "empty":
                request.setTrainerUsername("");
                break;
            case "short":
                request.setTrainerUsername("ab");
                break;
            case "long":
                request.setTrainerUsername("a".repeat(51));
                break;
            default:
                // Handle unexpected value if necessary
                break;
        }
    }

    private void setFirstNameValue(String value) {
        switch (value) {
            case "null":
                request.setTrainerFirstName(null);
                break;
            case "empty":
                request.setTrainerFirstName("");
                break;
            case "long":
                request.setTrainerFirstName("a".repeat(101));
                break;
            default:
                // Handle unexpected value if necessary
                break;
        }
    }

    private void setLastNameValue(String value) {
        switch (value) {
            case "null":
                request.setTrainerLastName(null);
                break;
            case "empty":
                request.setTrainerLastName("");
                break;
            case "long":
                request.setTrainerLastName("a".repeat(101));
                break;
            default:
                // Handle unexpected value if necessary
                break;
        }
    }

    private String getInvalidValue(String value) {
        switch (value) {
            case "null":
                return null;
            case "empty":
                return "";
            case "short":
                return "ab";
            case "long":
                return "a".repeat(51);
            default:
                return value; // Return original value if not a special keyword
        }
    }

    private String getInvalidYearValue(String value) {
        switch (value) {
            case "null":
                return null;
            case "empty":
                return "";
            case "invalid":
                return "invalid";
            case "tooLow":
                return "2024";
            case "tooHigh":
                return "2101";
            default:
                return value;
        }
    }

    private String getInvalidMonthValue(String value) {
        switch (value) {
            case "null":
                return null;
            case "empty":
                return "";
            case "invalid":
                return "INVALID";
            case "invalidNum":
                return "13";
            default:
                return value;
        }
    }

    private TrainerWorkingHours createExistingTrainer() {
        TrainerWorkingHours.Day day = TrainerWorkingHours.Day.builder()
                .day("15")
                .dailyWorkingHours(3.0f)
                .build();

        TrainerWorkingHours.Month month = TrainerWorkingHours.Month.builder()
                .month("JUNE")
                .monthlyWorkingHours(5.0f)
                .days(new ArrayList<>(List.of(day)))
                .build();

        TrainerWorkingHours.Year year = TrainerWorkingHours.Year.builder()
                .year("2025")
                .months(new ArrayList<>(List.of(month)))
                .build();

        return TrainerWorkingHours.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .years(new ArrayList<>(List.of(year)))
                .build();
    }

    private TrainerWorkingHours createTrainerWithExistingHours(int hours) {
        TrainerWorkingHours.Day day = TrainerWorkingHours.Day.builder()
                .day("15")
                .dailyWorkingHours((float) hours)
                .build();

        TrainerWorkingHours.Month month = TrainerWorkingHours.Month.builder()
                .month("JUNE")
                .monthlyWorkingHours((float) hours)
                .days(new ArrayList<>(List.of(day)))
                .build();

        TrainerWorkingHours.Year year = TrainerWorkingHours.Year.builder()
                .year("2025")
                .months(new ArrayList<>(List.of(month)))
                .build();

        return TrainerWorkingHours.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .years(new ArrayList<>(List.of(year)))
                .build();
    }

    private TrainerWorkingHours createTrainerWithNumericMonth() {
        TrainerWorkingHours.Day day = TrainerWorkingHours.Day.builder()
                .day("15")
                .dailyWorkingHours(3.0f)
                .build();

        TrainerWorkingHours.Month month = TrainerWorkingHours.Month.builder()
                .month("6") // Numeric month
                .monthlyWorkingHours(5.0f)
                .days(new ArrayList<>(List.of(day)))
                .build();

        TrainerWorkingHours.Year year = TrainerWorkingHours.Year.builder()
                .year("2025")
                .months(new ArrayList<>(List.of(month)))
                .build();

        return TrainerWorkingHours.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .years(new ArrayList<>(List.of(year)))
                .build();
    }
}
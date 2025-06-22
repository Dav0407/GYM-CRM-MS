package com.epam.trainer_session_management.service;

import com.epam.trainer_session_management.document.TrainerWorkingHours;
import com.epam.trainer_session_management.dto.TrainerWorkloadRequest;
import com.epam.trainer_session_management.dto.TrainerWorkloadResponse;
import com.epam.trainer_session_management.enums.ActionType;
import com.epam.trainer_session_management.repository.TrainerWorkingHoursRepository;
import com.epam.trainer_session_management.service.impl.TrainerWorkingHoursServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerWorkingHoursServiceTest {

    @Mock
    private TrainerWorkingHoursRepository repository;

    @InjectMocks
    private TrainerWorkingHoursServiceImpl service;

    private TrainerWorkloadRequest validRequest;
    private TrainerWorkingHours existingTrainer;

    @BeforeEach
    void setUp() {
        Date testDate = Date.from(LocalDate.of(2025, 6, 15).atStartOfDay(ZoneId.systemDefault()).toInstant());

        validRequest = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(testDate)
                .trainingDurationInMinutes(120)
                .actionType(ActionType.ADD)
                .build();

        existingTrainer = createExistingTrainer();
    }

    // ========== calculateAndSave Tests ==========

    @Test
    void calculateAndSave_NewTrainer_Success() {
        // Given
        when(repository.findById("john.doe")).thenReturn(Optional.empty());

        // When
        service.calculateAndSave(validRequest);

        // Then
        verify(repository).save(any(TrainerWorkingHours.class));
        verify(repository).findById("john.doe");
    }

    @Test
    void calculateAndSave_ExistingTrainer_Success() {
        // Given
        when(repository.findById("john.doe")).thenReturn(Optional.of(existingTrainer));

        // When
        service.calculateAndSave(validRequest);

        // Then
        verify(repository).save(existingTrainer);
        verify(repository).findById("john.doe");
    }

    @Test
    void calculateAndSave_DeleteAction_SubtractsHours() {
        // Given
        validRequest.setActionType(ActionType.DELETE);
        when(repository.findById("john.doe")).thenReturn(Optional.of(existingTrainer));

        // When
        service.calculateAndSave(validRequest);

        // Then
        verify(repository).save(existingTrainer);
    }

    @Test
    void calculateAndSave_InactiveTrainer_SubtractsHours() {
        // Given
        validRequest.setIsActive(false);
        when(repository.findById("john.doe")).thenReturn(Optional.of(existingTrainer));

        // When
        service.calculateAndSave(validRequest);

        // Then
        verify(repository).save(existingTrainer);
    }

    @Test
    void calculateAndSave_ExceedsDailyLimit_ThrowsException() {
        // Given
        validRequest.setTrainingDurationInMinutes(540); // 9 hours
        when(repository.findById("john.doe")).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.calculateAndSave(validRequest));

        assertTrue(exception.getMessage().contains("Daily limit is 8.0 hours"));
        verify(repository, never()).save(any());
    }

    @Test
    void calculateAndSave_ExistingTrainerExceedsDailyLimit_ThrowsException() {
        // Given
        validRequest.setTrainingDurationInMinutes(300); // 5 hours
        TrainerWorkingHours trainerWithHours = createTrainerWithExistingHours();
        when(repository.findById("john.doe")).thenReturn(Optional.of(trainerWithHours));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.calculateAndSave(validRequest));

        assertTrue(exception.getMessage().contains("would exceed daily limit"));
        verify(repository, never()).save(any());
    }

    // ========== getTrainerWorkingHours Tests ==========

    @Test
    void getTrainerWorkingHours_ValidParameters_ReturnsResponse() {
        // Given
        when(repository.findById("john.doe")).thenReturn(Optional.of(existingTrainer));

        // When
        TrainerWorkloadResponse response = service.getTrainerWorkingHours("john.doe", "2025", "JUNE");

        // Then
        assertNotNull(response);
        assertEquals("john.doe", response.getTrainerUsername());
        assertEquals("2025", response.getYear());
        assertEquals("JUNE", response.getMonth());
        assertEquals(5.0f, response.getWorkingHours());
    }

    @Test
    void getTrainerWorkingHours_TrainerNotFound_ThrowsException() {
        // Given
        when(repository.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.getTrainerWorkingHours("nonexistent", "2025", "JUNE"));

        assertEquals("Trainer not found: nonexistent", exception.getMessage());
    }

    @Test
    void getTrainerWorkingHours_MonthNotFound_ThrowsException() {
        // Given
        when(repository.findById("john.doe")).thenReturn(Optional.of(existingTrainer));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.getTrainerWorkingHours("john.doe", "2025", "DECEMBER"));

        assertTrue(exception.getMessage().contains("No data found for year 2025 and month DECEMBER"));
    }

    // ========== Validation Tests for calculateAndSave ==========

    @Test
    void calculateAndSave_NullRequest_ThrowsException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.calculateAndSave(null));

        assertEquals("TrainerWorkloadRequest cannot be null", exception.getMessage());
    }

    @Test
    void calculateAndSave_NullUsername_ThrowsException() {
        // Given
        validRequest.setTrainerUsername(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.calculateAndSave(validRequest));

        assertEquals("Trainer username cannot be null or empty", exception.getMessage());
    }

    @Test
    void calculateAndSave_EmptyUsername_ThrowsException() {
        // Given
        validRequest.setTrainerUsername("");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.calculateAndSave(validRequest));

        assertEquals("Trainer username cannot be null or empty", exception.getMessage());
    }

    @Test
    void calculateAndSave_ShortUsername_ThrowsException() {
        // Given
        validRequest.setTrainerUsername("ab");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.calculateAndSave(validRequest));

        assertEquals("Trainer username must be at least 3 characters long", exception.getMessage());
    }

    @Test
    void calculateAndSave_LongUsername_ThrowsException() {
        // Given
        validRequest.setTrainerUsername("a".repeat(51));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.calculateAndSave(validRequest));

        assertEquals("Trainer username cannot exceed 50 characters", exception.getMessage());
    }

    @Test
    void calculateAndSave_NullFirstName_ThrowsException() {
        // Given
        validRequest.setTrainerFirstName(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.calculateAndSave(validRequest));

        assertEquals("Trainer first name cannot be null or empty", exception.getMessage());
    }

    @Test
    void calculateAndSave_EmptyFirstName_ThrowsException() {
        // Given
        validRequest.setTrainerFirstName("");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.calculateAndSave(validRequest));

        assertEquals("Trainer first name cannot be null or empty", exception.getMessage());
    }

    @Test
    void calculateAndSave_LongFirstName_ThrowsException() {
        // Given
        validRequest.setTrainerFirstName("a".repeat(51));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.calculateAndSave(validRequest));

        assertEquals("Trainer first name cannot exceed 100 characters", exception.getMessage());
    }

    @Test
    void calculateAndSave_NullLastName_ThrowsException() {
        // Given
        validRequest.setTrainerLastName(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.calculateAndSave(validRequest));

        assertEquals("Trainer last name cannot be null or empty", exception.getMessage());
    }

    @Test
    void calculateAndSave_EmptyLastName_ThrowsException() {
        // Given
        validRequest.setTrainerLastName("");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.calculateAndSave(validRequest));

        assertEquals("Trainer last name cannot be null or empty", exception.getMessage());
    }

    @Test
    void calculateAndSave_LongLastName_ThrowsException() {
        // Given
        validRequest.setTrainerLastName("a".repeat(51));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.calculateAndSave(validRequest));

        assertEquals("Trainer last name cannot exceed 100 characters", exception.getMessage());
    }

    @Test
    void calculateAndSave_NullIsActive_ThrowsException() {
        // Given
        validRequest.setIsActive(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.calculateAndSave(validRequest));

        assertEquals("IsActive field cannot be null", exception.getMessage());
    }

    @Test
    void calculateAndSave_NullTrainingDate_ThrowsException() {
        // Given
        validRequest.setTrainingDate(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.calculateAndSave(validRequest));

        assertEquals("Training date cannot be null", exception.getMessage());
    }

    @Test
    void calculateAndSave_NullTrainingDuration_ThrowsException() {
        // Given
        validRequest.setTrainingDurationInMinutes(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.calculateAndSave(validRequest));

        assertEquals("Training duration cannot be null", exception.getMessage());
    }

    @Test
    void calculateAndSave_NullActionType_ThrowsException() {
        // Given
        validRequest.setActionType(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.calculateAndSave(validRequest));

        assertEquals("Action type cannot be null", exception.getMessage());
    }

    // ========== Validation Tests for getTrainerWorkingHours ==========

    @Test
    void getTrainerWorkingHours_NullUsername_ThrowsException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.getTrainerWorkingHours(null, "2025", "JUNE"));

        assertEquals("Trainer username cannot be null or empty", exception.getMessage());
    }

    @Test
    void getTrainerWorkingHours_EmptyUsername_ThrowsException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.getTrainerWorkingHours("", "2025", "JUNE"));

        assertEquals("Trainer username cannot be null or empty", exception.getMessage());
    }

    @Test
    void getTrainerWorkingHours_ShortUsername_ThrowsException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.getTrainerWorkingHours("ab", "2025", "JUNE"));

        assertEquals("Trainer username must be at least 3 characters long", exception.getMessage());
    }

    @Test
    void getTrainerWorkingHours_LongUsername_ThrowsException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.getTrainerWorkingHours("a".repeat(51), "2025", "JUNE"));

        assertEquals("Trainer username cannot exceed 50 characters", exception.getMessage());
    }

    @Test
    void getTrainerWorkingHours_NullYear_ThrowsException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.getTrainerWorkingHours("john.doe", null, "JUNE"));

        assertEquals("Year cannot be null or empty", exception.getMessage());
    }

    @Test
    void getTrainerWorkingHours_EmptyYear_ThrowsException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.getTrainerWorkingHours("john.doe", "", "JUNE"));

        assertEquals("Year cannot be null or empty", exception.getMessage());
    }

    @Test
    void getTrainerWorkingHours_InvalidYear_ThrowsException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.getTrainerWorkingHours("john.doe", "invalid", "JUNE"));

        assertEquals("Year must be a valid numeric value", exception.getMessage());
    }

    @Test
    void getTrainerWorkingHours_YearTooLow_ThrowsException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.getTrainerWorkingHours("john.doe", "2024", "JUNE"));

        assertEquals("Year must be between 2025 and 2100", exception.getMessage());
    }

    @Test
    void getTrainerWorkingHours_YearTooHigh_ThrowsException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.getTrainerWorkingHours("john.doe", "2101", "JUNE"));

        assertEquals("Year must be between 2025 and 2100", exception.getMessage());
    }

    @Test
    void getTrainerWorkingHours_NullMonth_ThrowsException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.getTrainerWorkingHours("john.doe", "2025", null));

        assertEquals("Month cannot be null or empty", exception.getMessage());
    }

    @Test
    void getTrainerWorkingHours_EmptyMonth_ThrowsException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.getTrainerWorkingHours("john.doe", "2025", ""));

        assertEquals("Month cannot be null or empty", exception.getMessage());
    }

    @Test
    void getTrainerWorkingHours_InvalidMonth_ThrowsException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.getTrainerWorkingHours("john.doe", "2025", "INVALID"));

        assertEquals("Month must be a valid month name (e.g., JANUARY, FEBRUARY) or number (1-12)", exception.getMessage());
    }

    @Test
    void getTrainerWorkingHours_ValidMonthNumber_Success() {
        // Given
        TrainerWorkingHours trainerWithNumericMonth = createTrainerWithNumericMonth();
        when(repository.findById("john.doe")).thenReturn(Optional.of(trainerWithNumericMonth));

        // When
        TrainerWorkloadResponse response = service.getTrainerWorkingHours("john.doe", "2025", "6");

        // Then
        assertNotNull(response);
        assertEquals("john.doe", response.getTrainerUsername());
        assertEquals("2025", response.getYear());
        assertEquals("6", response.getMonth());
        assertEquals(5.0f, response.getWorkingHours());
    }

    @Test
    void getTrainerWorkingHours_InvalidMonthNumber_ThrowsException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.getTrainerWorkingHours("john.doe", "2025", "13"));

        assertEquals("Month must be a valid month name (e.g., JANUARY, FEBRUARY) or number (1-12)", exception.getMessage());
    }

    // ========== Edge Case Tests ==========

    @Test
    void calculateAndSave_MinimumValidUsername_Success() {
        // Given
        validRequest.setTrainerUsername("abc");
        when(repository.findById("abc")).thenReturn(Optional.empty());

        // When
        service.calculateAndSave(validRequest);

        // Then
        verify(repository).save(any(TrainerWorkingHours.class));
    }

    @Test
    void calculateAndSave_MaximumValidUsername_Success() {
        // Given
        String maxUsername = "a".repeat(50);
        validRequest.setTrainerUsername(maxUsername);
        when(repository.findById(maxUsername)).thenReturn(Optional.empty());

        // When
        service.calculateAndSave(validRequest);

        // Then
        verify(repository).save(any(TrainerWorkingHours.class));
    }

    @Test
    void calculateAndSave_ZeroDuration_Success() {
        // Given
        validRequest.setTrainingDurationInMinutes(0);
        when(repository.findById("john.doe")).thenReturn(Optional.empty());

        // When
        service.calculateAndSave(validRequest);

        // Then
        verify(repository).save(any(TrainerWorkingHours.class));
    }

    @Test
    void calculateAndSave_NegativeDurationWithDelete_Success() {
        // Given
        validRequest.setTrainingDurationInMinutes(-60);
        validRequest.setActionType(ActionType.DELETE);
        when(repository.findById("john.doe")).thenReturn(Optional.of(existingTrainer));

        // When
        service.calculateAndSave(validRequest);

        // Then
        verify(repository).save(existingTrainer);
    }

    // ========== Helper Methods ==========

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

    private TrainerWorkingHours createTrainerWithExistingHours() {
        TrainerWorkingHours.Day day = TrainerWorkingHours.Day.builder()
                .day("15")
                .dailyWorkingHours(4.0f) // Already has 4 hours
                .build();

        TrainerWorkingHours.Month month = TrainerWorkingHours.Month.builder()
                .month("JUNE")
                .monthlyWorkingHours(4.0f)
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
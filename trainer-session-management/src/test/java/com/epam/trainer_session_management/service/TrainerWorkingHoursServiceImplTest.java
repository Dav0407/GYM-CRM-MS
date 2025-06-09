package com.epam.trainer_session_management.service;


import com.epam.trainer_session_management.dto.TrainerWorkloadRequest;
import com.epam.trainer_session_management.dto.TrainerWorkloadResponse;
import com.epam.trainer_session_management.enums.ActionType;
import com.epam.trainer_session_management.service.impl.TrainerWorkingHoursServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class TrainerWorkingHoursServiceImplTest {

    private TrainerWorkingHoursServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TrainerWorkingHoursServiceImpl();
        clearTrainerWorkloadMap();
    }

    private void clearTrainerWorkloadMap() {

        try {
            var field = TrainerWorkingHoursServiceImpl.class.getDeclaredField("TRAINER_WORKLOAD_MAP");
            field.setAccessible(true);
            ((java.util.Map<?, ?>) field.get(null)).clear();
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear trainer workload map", e);
        }
    }

    @Test
    @DisplayName("Should create new trainer record when trainer doesn't exist")
    void shouldCreateNewTrainerRecord() {

        TrainerWorkloadRequest request = createWorkloadRequest(
                "john.doe", "John", "Doe", true,
                createDate(2024, 3, 15), 120, ActionType.ADD
        );

        TrainerWorkloadResponse response = service.calculateAndSave(request);

        assertNotNull(response);
        assertEquals("john.doe", response.getTrainerUsername());
        assertEquals("2024", response.getYear());
        assertEquals("MARCH", response.getMonth());
        assertEquals(2.0f, response.getWorkingHours());
    }

    @Test
    @DisplayName("Should add hours to existing trainer record")
    void shouldAddHoursToExistingTrainer() {

        TrainerWorkloadRequest initialRequest = createWorkloadRequest(
                "jane.smith", "Jane", "Smith", true,
                createDate(2024, 3, 15), 60, ActionType.ADD
        );
        service.calculateAndSave(initialRequest);

        TrainerWorkloadRequest additionalRequest = createWorkloadRequest(
                "jane.smith", "Jane", "Smith", true,
                createDate(2024, 3, 20), 90, ActionType.ADD
        );
        TrainerWorkloadResponse response = service.calculateAndSave(additionalRequest);

        assertEquals("jane.smith", response.getTrainerUsername());
        assertEquals("2024", response.getYear());
        assertEquals("MARCH", response.getMonth());
        assertEquals(2.5f, response.getWorkingHours()); // 1.0 + 1.5 hours
    }

    @Test
    @DisplayName("Should subtract hours when action type is DELETE")
    void shouldSubtractHoursOnDelete() {

        TrainerWorkloadRequest initialRequest = createWorkloadRequest(
                "bob.wilson", "Bob", "Wilson", true,
                createDate(2024, 4, 10), 180, ActionType.ADD
        );
        service.calculateAndSave(initialRequest);

        TrainerWorkloadRequest deleteRequest = createWorkloadRequest(
                "bob.wilson", "Bob", "Wilson", true,
                createDate(2024, 4, 15), 60, ActionType.DELETE
        );
        TrainerWorkloadResponse response = service.calculateAndSave(deleteRequest);

        assertEquals("bob.wilson", response.getTrainerUsername());
        assertEquals("2024", response.getYear());
        assertEquals("APRIL", response.getMonth());
        assertEquals(2.0f, response.getWorkingHours()); // 3.0 - 1.0 hours
    }

    @Test
    @DisplayName("Should treat inactive trainer as DELETE even with ADD action type")
    void shouldTreatInactiveTrainerAsDelete() {

        TrainerWorkloadRequest initialRequest = createWorkloadRequest(
                "alice.brown", "Alice", "Brown", true,
                createDate(2024, 5, 5), 120, ActionType.ADD
        );
        service.calculateAndSave(initialRequest);

        TrainerWorkloadRequest inactiveRequest = createWorkloadRequest(
                "alice.brown", "Alice", "Brown", false, // inactive
                createDate(2024, 5, 10), 60, ActionType.ADD
        );
        TrainerWorkloadResponse response = service.calculateAndSave(inactiveRequest);

        assertEquals("alice.brown", response.getTrainerUsername());
        assertEquals("2024", response.getYear());
        assertEquals("MAY", response.getMonth());
        assertEquals(1.0f, response.getWorkingHours());
    }

    @Test
    @DisplayName("Should prevent negative working hours")
    void shouldPreventNegativeWorkingHours() {

        TrainerWorkloadRequest initialRequest = createWorkloadRequest(
                "charlie.davis", "Charlie", "Davis", true,
                createDate(2024, 6, 1), 60, ActionType.ADD
        );
        service.calculateAndSave(initialRequest);

        TrainerWorkloadRequest deleteRequest = createWorkloadRequest(
                "charlie.davis", "Charlie", "Davis", true,
                createDate(2024, 6, 5), 120, ActionType.DELETE
        );
        TrainerWorkloadResponse response = service.calculateAndSave(deleteRequest);

        assertEquals("charlie.davis", response.getTrainerUsername());
        assertEquals("2024", response.getYear());
        assertEquals("JUNE", response.getMonth());
        assertEquals(0.0f, response.getWorkingHours());
    }

    @Test
    @DisplayName("Should handle different months for same trainer")
    void shouldHandleDifferentMonths() {

        TrainerWorkloadRequest marchRequest = createWorkloadRequest(
                "david.lee", "David", "Lee", true,
                createDate(2024, 3, 15), 120, ActionType.ADD
        );
        service.calculateAndSave(marchRequest);

        TrainerWorkloadRequest aprilRequest = createWorkloadRequest(
                "david.lee", "David", "Lee", true,
                createDate(2024, 4, 15), 90, ActionType.ADD
        );
        TrainerWorkloadResponse response = service.calculateAndSave(aprilRequest);

        assertEquals("david.lee", response.getTrainerUsername());
        assertEquals("2024", response.getYear());
        assertEquals("APRIL", response.getMonth());
        assertEquals(1.5f, response.getWorkingHours());
    }

    @Test
    @DisplayName("Should handle different years for same trainer")
    void shouldHandleDifferentYears() {

        TrainerWorkloadRequest request2024 = createWorkloadRequest(
                "emma.taylor", "Emma", "Taylor", true,
                createDate(2024, 3, 15), 120, ActionType.ADD
        );
        service.calculateAndSave(request2024);

        TrainerWorkloadRequest request2025 = createWorkloadRequest(
                "emma.taylor", "Emma", "Taylor", true,
                createDate(2025, 3, 15), 90, ActionType.ADD
        );
        TrainerWorkloadResponse response = service.calculateAndSave(request2025);

        assertEquals("emma.taylor", response.getTrainerUsername());
        assertEquals("2025", response.getYear());
        assertEquals("MARCH", response.getMonth());
        assertEquals(1.5f, response.getWorkingHours());
    }

    @Test
    @DisplayName("Should retrieve trainer working hours successfully")
    void shouldRetrieveTrainerWorkingHours() {

        TrainerWorkloadRequest request = createWorkloadRequest(
                "frank.garcia", "Frank", "Garcia", true,
                createDate(2024, 7, 10), 150, ActionType.ADD
        );
        service.calculateAndSave(request);

        TrainerWorkloadResponse response = service.getTrainerWorkingHours("frank.garcia", "2024", "JULY");

        assertEquals("frank.garcia", response.getTrainerUsername());
        assertEquals("2024", response.getYear());
        assertEquals("JULY", response.getMonth());
        assertEquals(2.5f, response.getWorkingHours());
    }

    @Test
    @DisplayName("Should throw exception when trainer not found")
    void shouldThrowExceptionWhenTrainerNotFound() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.getTrainerWorkingHours("nonexistent.trainer", "2024", "JANUARY")
        );

        assertEquals("Trainer not found: nonexistent.trainer", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when year/month combination not found")
    void shouldThrowExceptionWhenYearMonthNotFound() {

        TrainerWorkloadRequest request = createWorkloadRequest(
                "grace.martinez", "Grace", "Martinez", true,
                createDate(2024, 3, 15), 120, ActionType.ADD
        );
        service.calculateAndSave(request);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.getTrainerWorkingHours("grace.martinez", "2024", "APRIL")
        );

        assertTrue(exception.getMessage().contains("No data found for year 2024 and month APRIL"));
    }

    @Test
    @DisplayName("Should handle case insensitive month retrieval")
    void shouldHandleCaseInsensitiveMonthRetrieval() {

        TrainerWorkloadRequest request = createWorkloadRequest(
                "henry.rodriguez", "Henry", "Rodriguez", true,
                createDate(2024, 8, 15), 180, ActionType.ADD
        );
        service.calculateAndSave(request);

        TrainerWorkloadResponse response = service.getTrainerWorkingHours("henry.rodriguez", "2024", "august");

        assertEquals("henry.rodriguez", response.getTrainerUsername());
        assertEquals("2024", response.getYear());
        assertEquals("august", response.getMonth());
        assertEquals(3.0f, response.getWorkingHours());
    }

    @Test
    @DisplayName("Should convert training duration from minutes to hours correctly")
    void shouldConvertMinutesToHoursCorrectly() {

        TrainerWorkloadRequest request = createWorkloadRequest(
                "isabel.wilson", "Isabel", "Wilson", true,
                createDate(2024, 9, 20), 90, ActionType.ADD
        );

        TrainerWorkloadResponse response = service.calculateAndSave(request);

        assertEquals(1.5f, response.getWorkingHours());
    }

    // Helper methods
    private TrainerWorkloadRequest createWorkloadRequest(String username, String firstName, String lastName,
                                                         Boolean isActive, Date trainingDate, Integer duration,
                                                         ActionType actionType) {
        return TrainerWorkloadRequest.builder()
                .trainerUsername(username)
                .trainerFirstName(firstName)
                .trainerLastName(lastName)
                .isActive(isActive)
                .trainingDate(trainingDate)
                .trainingDuration(duration)
                .actionType(actionType)
                .build();
    }

    private Date createDate(int year, int month, int day) {
        LocalDate localDate = LocalDate.of(year, month, day);
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
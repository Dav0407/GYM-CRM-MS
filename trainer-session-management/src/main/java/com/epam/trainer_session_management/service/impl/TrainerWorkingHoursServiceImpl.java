package com.epam.trainer_session_management.service.impl;

import com.epam.trainer_session_management.document.TrainerWorkingHours;
import com.epam.trainer_session_management.dto.TrainerWorkloadRequest;
import com.epam.trainer_session_management.dto.TrainerWorkloadResponse;
import com.epam.trainer_session_management.enums.ActionType;
import com.epam.trainer_session_management.repository.TrainerWorkingHoursRepository;
import com.epam.trainer_session_management.service.TrainerWorkingHoursService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainerWorkingHoursServiceImpl implements TrainerWorkingHoursService {

    private static final int MIN_YEAR = 2025;
    private static final int MAX_YEAR = 2100;
    private static final float DAILY_HOUR_LIMIT = 8.0F;

    private final TrainerWorkingHoursRepository repository;

    @Override
    public void calculateAndSave(TrainerWorkloadRequest request) {
        validateTrainerWorkloadRequest(request);

        LocalDate localDate = toLocalDate(request.getTrainingDate());
        String year = String.valueOf(localDate.getYear());
        String month = String.valueOf(localDate.getMonth());
        String day = String.valueOf(localDate.getDayOfMonth());
        String username = request.getTrainerUsername();

        float rawDurationHours = request.getTrainingDurationInMinutes() / 60.0F;
        boolean shouldSubtract = request.getActionType() == ActionType.DELETE || !request.getIsActive();
        float durationHours = shouldSubtract ? -rawDurationHours : rawDurationHours;

        TrainerWorkingHours existing = repository.findById(username).orElse(null);

        // Check for 8-hour a day limit (only for ADD operations)
        if (!shouldSubtract) {
            validateDailyHourLimit(existing, year, month, day, durationHours, username);
        }

        TrainerWorkingHours updated;
        if (existing == null) {
            updated = createNewTrainerRecord(request, year, month, day, durationHours);
        } else {
            updated = updateTrainerRecord(existing, year, month, day, durationHours);
        }

        repository.save(updated); // persist changes
    }

    @Override
    public TrainerWorkloadResponse getTrainerWorkingHours(String trainerUsername, String year, String month) {
        validateGetTrainerWorkingHoursParameters(trainerUsername, year, month);

        TrainerWorkingHours trainerWorkingHours = repository.findById(trainerUsername)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + trainerUsername));

        TrainerWorkingHours.Month monthEntry = trainerWorkingHours.getYears().stream()
                .filter(y -> y.getYear().equals(year))
                .flatMap(y -> y.getMonths().stream())
                .filter(m -> m.getMonth().equals(month.toUpperCase()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No data found for year " + year + " and month " + month));

        return TrainerWorkloadResponse.builder()
                .trainerUsername(trainerWorkingHours.getTrainerUsername())
                .year(year)
                .month(month)
                .workingHours(monthEntry.getMonthlyWorkingHours())
                .build();
    }

    private void validateDailyHourLimit(TrainerWorkingHours existing, String year, String month, String day, float additionalHours, String username) {
        if (existing == null) {
            // New trainer record, check if additional hours exceed the limit
            if (additionalHours > DAILY_HOUR_LIMIT) {
                throw new IllegalArgumentException(
                        String.format("Cannot add %.2f hours for trainer %s on %s-%s-%s. Daily limit is %.1f hours.",
                                additionalHours, username, year, month, day, DAILY_HOUR_LIMIT));
            }
            return;
        }

        // Find existing daily hours
        float currentDailyHours = existing.getYears().stream()
                .filter(y -> y.getYear().equals(year))
                .flatMap(y -> y.getMonths().stream())
                .filter(m -> m.getMonth().equals(month))
                .flatMap(m -> m.getDays().stream())
                .filter(d -> d.getDay().equals(day))
                .map(TrainerWorkingHours.Day::getDailyWorkingHours)
                .findFirst()
                .orElse(0.0F);

        float newTotal = currentDailyHours + additionalHours;

        if (newTotal > DAILY_HOUR_LIMIT) {
            throw new IllegalArgumentException(
                    String.format("Cannot add %.2f hours for trainer %s on %s-%s-%s. Current hours: %.2f, would exceed daily limit of %.1f hours.",
                            additionalHours, username, year, month, day, currentDailyHours, DAILY_HOUR_LIMIT));
        }
    }

    private static LocalDate toLocalDate(Date trainingDate) {
        return trainingDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private TrainerWorkingHours createNewTrainerRecord(TrainerWorkloadRequest request, String year, String month, String day, Float durationHours) {

        TrainerWorkingHours.Day newDay = TrainerWorkingHours.Day.builder()
                .day(day)
                .dailyWorkingHours(Math.max(0.0f, durationHours))
                .build();

        TrainerWorkingHours.Month newMonth = TrainerWorkingHours.Month.builder()
                .month(month)
                .monthlyWorkingHours(Math.max(0.0f, durationHours))
                .days(new ArrayList<>(List.of(newDay)))
                .build();

        TrainerWorkingHours.Year newYear = TrainerWorkingHours.Year.builder()
                .year(year)
                .months(new ArrayList<>(List.of(newMonth)))
                .build();

        return TrainerWorkingHours.builder()
                .trainerUsername(request.getTrainerUsername())
                .trainerFirstName(request.getTrainerFirstName())
                .trainerLastName(request.getTrainerLastName())
                .isActive(request.getIsActive())
                .years(new ArrayList<>(List.of(newYear)))
                .build();
    }

    private TrainerWorkingHours updateTrainerRecord(TrainerWorkingHours existing, String year, String month, String day, Float durationHours) {

        List<TrainerWorkingHours.Year> years = existing.getYears();
        TrainerWorkingHours.Year yearEntry = years.stream()
                .filter(y -> y.getYear().equals(year))
                .findFirst()
                .orElseGet(() -> {
                    TrainerWorkingHours.Year newYear = TrainerWorkingHours.Year.builder()
                            .year(year)
                            .months(new ArrayList<>())
                            .build();
                    years.add(newYear);
                    return newYear;
                });

        List<TrainerWorkingHours.Month> months = yearEntry.getMonths();
        TrainerWorkingHours.Month monthEntry = months.stream()
                .filter(m -> m.getMonth().equals(month))
                .findFirst()
                .orElseGet(() -> {
                    TrainerWorkingHours.Month newMonth = TrainerWorkingHours.Month.builder()
                            .month(month)
                            .monthlyWorkingHours(0.0f)
                            .days(new ArrayList<>())
                            .build();
                    months.add(newMonth);
                    return newMonth;
                });

        // Update monthly hours
        float newMonthlyTotal = monthEntry.getMonthlyWorkingHours() + durationHours;
        monthEntry.setMonthlyWorkingHours(Math.max(0.0f, newMonthlyTotal));

        List<TrainerWorkingHours.Day> days = monthEntry.getDays();
        if (days == null) {
            days = new ArrayList<>();
            monthEntry.setDays(days);
        }

        final List<TrainerWorkingHours.Day> finalDays = days;
        TrainerWorkingHours.Day dayEntry = days.stream()
                .filter(d -> d.getDay().equals(day))
                .findFirst()
                .orElseGet(() -> {
                    TrainerWorkingHours.Day newDay = TrainerWorkingHours.Day.builder()
                            .day(day)
                            .dailyWorkingHours(0.0f)
                            .build();
                    finalDays.add(newDay);
                    return newDay;
                });

        float newDailyTotal = dayEntry.getDailyWorkingHours() + durationHours;
        dayEntry.setDailyWorkingHours(Math.max(0.0f, newDailyTotal));

        return existing;
    }

    private TrainerWorkingHours.Month findMonth(TrainerWorkingHours trainer, String year, String month) {
        return trainer.getYears().stream()
                .filter(y -> y.getYear().equals(year))
                .flatMap(y -> y.getMonths().stream())
                .filter(m -> m.getMonth().equals(month))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Month not found for trainer"));
    }

    private void validateTrainerWorkloadRequest(TrainerWorkloadRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("TrainerWorkloadRequest cannot be null");
        }


        if (!StringUtils.hasText(request.getTrainerUsername())) {
            throw new IllegalArgumentException("Trainer username cannot be null or empty");
        }
        if (request.getTrainerUsername().trim().length() < 3) {
            throw new IllegalArgumentException("Trainer username must be at least 3 characters long");
        }
        if (request.getTrainerUsername().trim().length() > 50) {
            throw new IllegalArgumentException("Trainer username cannot exceed 50 characters");
        }


        if (!StringUtils.hasText(request.getTrainerFirstName())) {
            throw new IllegalArgumentException("Trainer first name cannot be null or empty");
        }
        if (request.getTrainerFirstName().trim().length() > 50) {
            throw new IllegalArgumentException("Trainer first name cannot exceed 100 characters");
        }


        if (!StringUtils.hasText(request.getTrainerLastName())) {
            throw new IllegalArgumentException("Trainer last name cannot be null or empty");
        }
        if (request.getTrainerLastName().trim().length() > 50) {
            throw new IllegalArgumentException("Trainer last name cannot exceed 100 characters");
        }


        if (request.getIsActive() == null) {
            throw new IllegalArgumentException("IsActive field cannot be null");
        }


        if (request.getTrainingDate() == null) {
            throw new IllegalArgumentException("Training date cannot be null");
        }


        if (request.getTrainingDurationInMinutes() == null) {
            throw new IllegalArgumentException("Training duration cannot be null");
        }


        if (request.getActionType() == null) {
            throw new IllegalArgumentException("Action type cannot be null");
        }
    }

    private void validateGetTrainerWorkingHoursParameters(String trainerUsername, String year, String month) {

        if (!StringUtils.hasText(trainerUsername)) {
            throw new IllegalArgumentException("Trainer username cannot be null or empty");
        }
        if (trainerUsername.trim().length() < 3) {
            throw new IllegalArgumentException("Trainer username must be at least 3 characters long");
        }
        if (trainerUsername.trim().length() > 50) {
            throw new IllegalArgumentException("Trainer username cannot exceed 50 characters");
        }


        if (!StringUtils.hasText(year)) {
            throw new IllegalArgumentException("Year cannot be null or empty");
        }


        try {
            int yearValue = Integer.parseInt(year);
            if (yearValue < MIN_YEAR || yearValue > MAX_YEAR) {
                throw new IllegalArgumentException("Year must be between " + MIN_YEAR + " and " + MAX_YEAR);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Year must be a valid numeric value");
        }


        if (!StringUtils.hasText(month)) {
            throw new IllegalArgumentException("Month cannot be null or empty");
        }


        if (!isValidMonth(month)) {
            throw new IllegalArgumentException("Month must be a valid month name (e.g., JANUARY, FEBRUARY) or number (1-12)");
        }
    }

    private boolean isValidMonth(String month) {
        try {
            // Try parsing as a month number (1-12)
            int monthNumber = Integer.parseInt(month);
            return monthNumber >= 1 && monthNumber <= 12;
        } catch (NumberFormatException e) {

            try {
                String upperMonth = month.toUpperCase();
                return upperMonth.equals("JANUARY") || upperMonth.equals("FEBRUARY") ||
                        upperMonth.equals("MARCH") || upperMonth.equals("APRIL") ||
                        upperMonth.equals("MAY") || upperMonth.equals("JUNE") ||
                        upperMonth.equals("JULY") || upperMonth.equals("AUGUST") ||
                        upperMonth.equals("SEPTEMBER") || upperMonth.equals("OCTOBER") ||
                        upperMonth.equals("NOVEMBER") || upperMonth.equals("DECEMBER");
            } catch (Exception ex) {
                return false;
            }
        }
    }
}
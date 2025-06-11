package com.epam.trainer_session_management.listener;

import com.epam.trainer_session_management.dto.TrainerWorkloadRequest;
import com.epam.trainer_session_management.service.TrainerWorkingHoursService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrainerHoursMessageListener {

    private final TrainerWorkingHoursService trainerWorkingHoursService;

    @JmsListener(destination = "${application.broker.destination}", containerFactory = "jmsListenerContainerFactory")
    public void onMessage(@Payload TrainerWorkloadRequest message, @Header("transactionId") String transactionId) {
        MDC.put("transactionId", transactionId);
        try {
            log.info("Message received: {}", message);

            // Simulate failure for testing DLQ
            if (message.getTrainerUsername() == null) {
                throw new IllegalArgumentException("Trainer ID is missing — cannot process message");
            }

            trainerWorkingHoursService.calculateAndSave(message);
        } finally {
            MDC.clear(); // Prevent logging context leak
        }
    }
}


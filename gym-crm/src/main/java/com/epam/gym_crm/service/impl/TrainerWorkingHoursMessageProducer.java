package com.epam.gym_crm.service.impl;

import com.epam.gym_crm.dto.request.TrainerWorkloadRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerWorkingHoursMessageProducer {

    @Value("${application.active_mq.broker.destination}")
    private String destination;

    private final JmsTemplate jmsTemplate;

    public void sendMessage(TrainerWorkloadRequest message) {
        log.info("Sending computeTrainerHours for {} to Service B", message.getTrainerUsername());
        jmsTemplate.convertAndSend(destination, message, message1 -> {
            message1.setStringProperty("transactionId", MDC.get("transactionId"));
            return message1;
        });
        log.info("Message : {} sent to Service B.", message);
    }
}

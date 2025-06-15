package com.epam.gym_crm.service;

import com.epam.gym_crm.dto.request.TrainerWorkloadRequest;
import com.epam.gym_crm.service.impl.TrainerWorkingHoursMessageProducer;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TrainerWorkingHoursMessageProducerTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private Message message;

    @InjectMocks
    private TrainerWorkingHoursMessageProducer messageProducer;

    @Captor
    private ArgumentCaptor<MessagePostProcessor> messagePostProcessorCaptor;

    @Captor
    private ArgumentCaptor<TrainerWorkloadRequest> messageCaptor;

    @Captor
    private ArgumentCaptor<String> destinationCaptor;

    private TrainerWorkloadRequest testRequest;
    private final String testDestination = "test.destination.queue";
    private final String testTransactionId = "test-transaction-123";

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(messageProducer, "destination", testDestination);

        String testTrainerUsername = "john.trainer";
        testRequest = TrainerWorkloadRequest.builder()
                .trainerUsername(testTrainerUsername)
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(new Date())
                .trainingDurationInMinutes(60)
                .actionType(TrainerWorkloadRequest.ActionType.ADD)
                .build();


        MDC.put("transactionId", testTransactionId);
    }

    @Test
    void sendMessage_ShouldSendMessageWithCorrectDestinationAndPayload() {

        messageProducer.sendMessage(testRequest);


        verify(jmsTemplate).convertAndSend(
                destinationCaptor.capture(),
                messageCaptor.capture(),
                any(MessagePostProcessor.class)
        );

        assertEquals(testDestination, destinationCaptor.getValue());
        assertEquals(testRequest, messageCaptor.getValue());
    }

    @Test
    void sendMessage_ShouldSetTransactionIdFromMDC() throws JMSException {

        messageProducer.sendMessage(testRequest);


        verify(jmsTemplate).convertAndSend(
                eq(testDestination),
                eq(testRequest),
                messagePostProcessorCaptor.capture()
        );

        MessagePostProcessor capturedProcessor = messagePostProcessorCaptor.getValue();
        Message processedMessage = capturedProcessor.postProcessMessage(message);

        verify(message).setStringProperty("transactionId", testTransactionId);
        assertEquals(message, processedMessage);
    }

    @Test
    void sendMessage_ShouldHandleNullTransactionIdInMDC() throws JMSException {

        MDC.remove("transactionId");


        messageProducer.sendMessage(testRequest);


        verify(jmsTemplate).convertAndSend(
                eq(testDestination),
                eq(testRequest),
                messagePostProcessorCaptor.capture()
        );

        MessagePostProcessor capturedProcessor = messagePostProcessorCaptor.getValue();
        capturedProcessor.postProcessMessage(message);

        verify(message).setStringProperty("transactionId", null);
    }

    @Test
    void sendMessage_ShouldPropagateJMSException() throws JMSException {

        doThrow(new JMSException("JMS Error")).when(message).setStringProperty(anyString(), anyString());
        assertThrows(JMSException.class, () -> {
            messageProducer.sendMessage(testRequest);

            verify(jmsTemplate).convertAndSend(
                    eq(testDestination),
                    eq(testRequest),
                    messagePostProcessorCaptor.capture()
            );

            MessagePostProcessor capturedProcessor = messagePostProcessorCaptor.getValue();
            capturedProcessor.postProcessMessage(message);
        });
    }

    @Test
    void sendMessage_ShouldCallJmsTemplateOnlyOnce() {

        messageProducer.sendMessage(testRequest);


        verify(jmsTemplate, times(1)).convertAndSend(
                anyString(),
                any(TrainerWorkloadRequest.class),
                any(MessagePostProcessor.class)
        );
    }

    @Test
    void sendMessage_ShouldWorkWithDifferentTrainerUsernames() {

        TrainerWorkloadRequest anotherRequest = TrainerWorkloadRequest.builder()
                .trainerUsername("jane.trainer")
                .trainerFirstName("Jane")
                .trainerLastName("Smith")
                .isActive(false)
                .trainingDate(new Date())
                .trainingDurationInMinutes(90)
                .actionType(TrainerWorkloadRequest.ActionType.DELETE)
                .build();


        messageProducer.sendMessage(anotherRequest);


        verify(jmsTemplate).convertAndSend(
                eq(testDestination),
                eq(anotherRequest),
                any(MessagePostProcessor.class)
        );
    }

    @Test
    void sendMessage_ShouldHandleAllFieldsInRequest() {

        Date testDate = new Date();
        TrainerWorkloadRequest complexRequest = TrainerWorkloadRequest.builder()
                .trainerUsername("complex.trainer")
                .trainerFirstName("Complex")
                .trainerLastName("Trainer")
                .isActive(true)
                .trainingDate(testDate)
                .trainingDurationInMinutes(120)
                .actionType(TrainerWorkloadRequest.ActionType.ADD)
                .build();


        messageProducer.sendMessage(complexRequest);


        verify(jmsTemplate).convertAndSend(
                eq(testDestination),
                messageCaptor.capture(),
                any(MessagePostProcessor.class)
        );

        TrainerWorkloadRequest capturedRequest = messageCaptor.getValue();
        assertEquals("complex.trainer", capturedRequest.getTrainerUsername());
        assertEquals("Complex", capturedRequest.getTrainerFirstName());
        assertEquals("Trainer", capturedRequest.getTrainerLastName());
        assertEquals(true, capturedRequest.getIsActive());
        assertEquals(testDate, capturedRequest.getTrainingDate());
        assertEquals(120, capturedRequest.getTrainingDurationInMinutes());
        assertEquals(TrainerWorkloadRequest.ActionType.ADD, capturedRequest.getActionType());
    }

    @Test
    void sendMessage_ShouldHandleDeleteActionType() {

        TrainerWorkloadRequest deleteRequest = TrainerWorkloadRequest.builder()
                .trainerUsername("delete.trainer")
                .trainerFirstName("Delete")
                .trainerLastName("Test")
                .isActive(false)
                .trainingDate(new Date())
                .trainingDurationInMinutes(45)
                .actionType(TrainerWorkloadRequest.ActionType.DELETE)
                .build();


        messageProducer.sendMessage(deleteRequest);


        verify(jmsTemplate).convertAndSend(
                eq(testDestination),
                messageCaptor.capture(),
                any(MessagePostProcessor.class)
        );

        assertEquals(TrainerWorkloadRequest.ActionType.DELETE,
                messageCaptor.getValue().getActionType());
    }
}
package com.epam.gym_crm.actuator;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class MetricsAspectTest {

    private MeterRegistry meterRegistry;
    private MetricsAspect metricsAspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        meterRegistry = new SimpleMeterRegistry();
        metricsAspect = new MetricsAspect(meterRegistry);
    }

    @Test
    void trackCustomEvent_SuccessfulExecution_RecordsMetrics() throws Throwable {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(joinPoint.proceed()).thenReturn("success");
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getDeclaringType()).thenReturn(TestController.class);
        when(methodSignature.getName()).thenReturn("testMethod");

        // Act
        Object result = metricsAspect.trackCustomEvent(joinPoint);

        // Assert
        assertEquals("success", result);

        // Verify counter
        Counter counter = meterRegistry.counter("event_counter",
                "method", "GET",
                "endpoint", "TestController.testMethod",
                "status", "success");
        assertEquals(0, counter.count());

        // Verify timer
        Timer timer = meterRegistry.timer("event_request_latency",
                "method", "GET",
                "endpoint", "TestController.testMethod",
                "status", "success");
        assertEquals(1, timer.count());
    }

    @Test
    void trackCustomEvent_Exception_RecordsErrorMetrics() throws Throwable {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/error");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(joinPoint.proceed()).thenThrow(new RuntimeException("Test error"));
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getDeclaringType()).thenReturn(TestController.class);
        when(methodSignature.getName()).thenReturn("errorMethod");

        // Act & Assert
        assertThrows(RuntimeException.class, () -> metricsAspect.trackCustomEvent(joinPoint));

        // Verify error counter
        Counter errorCounter = meterRegistry.counter("event_errors_total",
                "method", "POST",
                "endpoint", "TestController.errorMethod",
                "status", "500",
                "exception", "RuntimeException");
        assertEquals(1, errorCounter.count());
    }

    @Test
    void trackCustomEvent_ResponseStatusException_RecordsCorrectStatus() throws Throwable {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/notfound");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(joinPoint.proceed()).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getDeclaringType()).thenReturn(TestController.class);
        when(methodSignature.getName()).thenReturn("notFoundMethod");

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> metricsAspect.trackCustomEvent(joinPoint));

        // Verify status code
        Counter errorCounter = meterRegistry.counter("event_errors_total",
                "method", "PUT",
                "endpoint", "TestController.notFoundMethod",
                "status", "404 NOT_FOUND",
                "exception", "ResponseStatusException");
        assertEquals(1, errorCounter.count());
    }

    @Test
    void trackCustomEvent_NoRequestContext_StillWorks() throws Throwable {
        // Arrange - no request context set
        RequestContextHolder.resetRequestAttributes();

        when(joinPoint.proceed()).thenReturn("success");
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getDeclaringType()).thenReturn(TestController.class);
        when(methodSignature.getName()).thenReturn("noContextMethod");

        // Act
        Object result = metricsAspect.trackCustomEvent(joinPoint);

        // Assert
        assertEquals("success", result);

        // Verify default method tag
        Counter counter = meterRegistry.counter("event_counter",
                "method", "UNKNOWN",
                "endpoint", "TestController.noContextMethod",
                "status", "success");
        assertEquals(0, counter.count());
    }

    // Test controller class for the aspect
    private static class TestController {
        public void testMethod() {}
        public void errorMethod() {}
        public void notFoundMethod() {}
        public void noContextMethod() {}
    }
}
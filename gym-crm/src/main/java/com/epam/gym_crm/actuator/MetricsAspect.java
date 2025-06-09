package com.epam.gym_crm.actuator;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Aspect
@Component
public class MetricsAspect {

    private final MeterRegistry meterRegistry;

    @Autowired
    public MetricsAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Around("execution(* com.epam.gym_crm.controller.*.*(..))")
    public Object trackCustomEvent(ProceedingJoinPoint joinPoint) throws Throwable {

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        String httpMethod = request != null ? request.getMethod() : "UNKNOWN";

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String controllerName = methodSignature.getDeclaringType().getSimpleName();
        String methodName = methodSignature.getName();
        String endpoint = controllerName + "." + methodName;

        List<Tag> tags = new ArrayList<>();
        tags.add(Tag.of("method", httpMethod));
        tags.add(Tag.of("endpoint", endpoint));

        Timer.Sample sample = Timer.start();

        try {

            Object result = joinPoint.proceed();

            Counter successCounter = Counter.builder("event_counter")
                    .description("A counter for tracking successful events")
                    .tags(tags)
                    .register(meterRegistry);
            successCounter.increment();

            tags.add(Tag.of("status", "success"));

            Timer timer = Timer.builder("event_request_latency")
                    .description("Latency of controller requests in seconds")
                    .tags(tags)
                    .register(meterRegistry);
            sample.stop(timer);

            return result;
        } catch (Exception e) {

            String statusCode = determineStatusCode(e);
            tags.add(Tag.of("status", statusCode));
            tags.add(Tag.of("exception", e.getClass().getSimpleName()));


            Counter errorCounter = Counter.builder("event_errors_total")
                    .description("A counter for tracking failed events")
                    .tags(tags)
                    .register(meterRegistry);
            errorCounter.increment();

            Timer timer = Timer.builder("event_request_latency")
                    .description("Latency of controller requests in seconds")
                    .tags(tags)
                    .register(meterRegistry);
            sample.stop(timer);

            throw e;
        }
    }

    private String determineStatusCode(Exception e) {
        if (e instanceof ResponseStatusException) {
            HttpStatusCode status = ((ResponseStatusException) e).getStatusCode();
            return status.toString();
        }
        return "500";
    }
}
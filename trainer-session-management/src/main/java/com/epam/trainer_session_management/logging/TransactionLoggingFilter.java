package com.epam.trainer_session_management.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TransactionLoggingFilter extends OncePerRequestFilter {

    private static final String TRANSACTION_ID = "X-Transaction-Id";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        String transactionId = request.getHeader(TRANSACTION_ID);
        MDC.put("transactionId", transactionId);

        try {
            System.out.printf("Transaction Start: [%s] %s %s%n", transactionId, request.getMethod(), request.getRequestURI());

            filterChain.doFilter(request, response);

            System.out.printf("Transaction End: [%s] Status: %d%n", transactionId, response.getStatus());
        } finally {
            MDC.remove(TRANSACTION_ID);
        }
    }
}

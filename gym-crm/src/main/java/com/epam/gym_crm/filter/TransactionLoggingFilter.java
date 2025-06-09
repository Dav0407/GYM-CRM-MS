package com.epam.gym_crm.filter;

import com.epam.gym_crm.logging_context.TransactionContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionLoggingFilter extends OncePerRequestFilter {

    private static final String TRANSACTION_ID = "transactionId";

    private final TransactionContext transactionContext;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        String transactionId = UUID.randomUUID().toString();
        MDC.put(TRANSACTION_ID, transactionId);

        transactionContext.setTransactionId(transactionId);

        try {
            System.out.printf("Transaction Start: [%s] %s %s%n", transactionId, request.getMethod(), request.getRequestURI());

            filterChain.doFilter(request, response);

            System.out.printf("Transaction End: [%s] Status: %d%n", transactionId, response.getStatus());
        } finally {
            MDC.remove(TRANSACTION_ID);
        }
    }
}

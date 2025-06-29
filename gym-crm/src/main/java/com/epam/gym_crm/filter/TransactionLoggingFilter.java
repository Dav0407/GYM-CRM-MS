package com.epam.gym_crm.filter;

import com.epam.gym_crm.logging_context.TransactionContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
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
            log.info("Transaction Start: {} {} {}", transactionId, request.getMethod(), request.getRequestURI());

            filterChain.doFilter(request, response);

            log.info("Transaction End: {} Status: {}", transactionId, response.getStatus());
        } finally {
            MDC.remove(TRANSACTION_ID);
        }
    }
}

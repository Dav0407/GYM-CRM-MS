package com.epam.gym_crm.filter;

import com.epam.gym_crm.dto.response.ExceptionResponseDTO;
import com.epam.gym_crm.exception.TokenIsBlacklistedException;
import com.epam.gym_crm.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.epam.gym_crm.handler.BusinessErrorCodes.RESOURCE_NOT_FOUND;
import static com.epam.gym_crm.handler.BusinessErrorCodes.USER_UNAUTHORIZED;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        final String jwtAccessToken;

        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwtAccessToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(jwtAccessToken);

        // Here once the user is parsing the JWT in the Header we are loading the user to Security Context Holder
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Validate token and check if blacklisted
                if (jwtService.isAccessTokenValid(jwtAccessToken, userDetails) || jwtService.isRefreshTokenValid(jwtAccessToken, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                } else {
                    var exception = new TokenIsBlacklistedException("Your authentication token is blacklisted or expired. Please log in again.");
                    log.error("Invalid JWT token: {}", jwtAccessToken, exception);
                    throw exception;
                }
                // Here I am doing the same thing as in GlobalExceptionHandler but manually since this is not the MVC layer
            } catch (UsernameNotFoundException exception) {
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.setContentType("application/json");
                response.getWriter().write(
                        new ObjectMapper().writeValueAsString(ExceptionResponseDTO.builder()
                                .businessErrorCode(RESOURCE_NOT_FOUND.getCode())
                                .businessErrorDescription(RESOURCE_NOT_FOUND.getDescription())
                                .errorMessage(exception.getMessage())
                                .build())
                );
                return;
            } catch (TokenIsBlacklistedException exception) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json");
                response.getWriter().write(
                        new ObjectMapper().writeValueAsString(ExceptionResponseDTO.builder()
                                .businessErrorCode(USER_UNAUTHORIZED.getCode())
                                .businessErrorDescription(USER_UNAUTHORIZED.getDescription())
                                .errorMessage(exception.getMessage())
                                .build())
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}

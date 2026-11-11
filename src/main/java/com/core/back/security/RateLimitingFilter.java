package com.core.back.security;

import com.core.back.payload.GenericResponse;
import com.core.back.payload.SecurityErrorDetails;
import com.core.back.service.LoginRateLimiterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final LoginRateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper;

    public RateLimitingFilter(LoginRateLimiterService rateLimiterService, ObjectMapper objectMapper) {
        this.rateLimiterService = rateLimiterService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equalsIgnoreCase(request.getMethod()) && "/v0/signin".equals(request.getServletPath()));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            if (!rateLimiterService.isAllowed(request)) {
                writeError(
                        request,
                        response,
                        HttpStatus.TOO_MANY_REQUESTS.value(),
                        "Too Many Requests",
                        "You have exceeded the login attempt limit. Please try again in 1 minute.");
                return;
            }
        } catch (DataAccessException exception) {
            writeError(
                    request,
                    response,
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Service Unavailable",
                    "The authentication service is temporarily unavailable.");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void writeError(
            HttpServletRequest request,
            HttpServletResponse response,
            int status,
            String error,
            String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(
                response.getOutputStream(),
                GenericResponse.error(
                        message,
                        new SecurityErrorDetails(
                                Instant.now(),
                                status,
                                error,
                                request.getServletPath())));
    }

}

package com.core.back.exception;

import com.core.back.payload.GenericResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new GenericResponse<>(false, "Validation failed for one or more fields", errors));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<GenericResponse<Object>> handleBusinessValidation(ValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new GenericResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<GenericResponse<Object>> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new GenericResponse<>(false, "Invalid credentials", null));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<GenericResponse<Object>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new GenericResponse<>(false, "Access denied", null));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<GenericResponse<Object>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new GenericResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<GenericResponse<Object>> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new GenericResponse<>(false, ex.getMessage(), null));

    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<GenericResponse<Object>> handleDuplicateKey(DuplicateKeyException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new GenericResponse<>(
                        false,
                        "A record with that value already exists.",
                        null
                ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<GenericResponse<Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new GenericResponse<>(false, "The request violates a data constraint.", null));
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<GenericResponse<Object>> handleDatabaseResourceFailure(DataAccessResourceFailureException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new GenericResponse<>(false, "The database is temporarily unavailable.", null));
    }

    @ExceptionHandler(QueryTimeoutException.class)
    public ResponseEntity<GenericResponse<Object>> handleQueryTimeout(QueryTimeoutException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new GenericResponse<>(false, "The database request timed out. Please try again later.", null));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<GenericResponse<Object>> handleDataAccess(DataAccessException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new GenericResponse<>(false, "A database operation failed.", null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse<Object>> handleAllExceptions(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new GenericResponse<>(false, "An unexpected error occurred", null));
    }
    
}
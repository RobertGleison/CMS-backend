package com.backend.Netflix.exceptions;

import com.google.firebase.database.DatabaseException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

/**
 * Central exception handler for the Netflix backend application.
 * Provides centralized error handling and personalized response formatting for various exceptions.
 * Uses {@link StandardException} as the standard response of exception message.
 */
@ControllerAdvice
public class ExceptionHandlerCentral {


    /**
     * Handles exceptions when requested media content is not found.
     * Returns a 404 NOT_FOUND status with detailed error information.
     * @param exception The thrown MediaNotFoundException
     * @param request The HTTP request that triggered the exception
     * @return ResponseEntity containing standardized error details
     */
    @ExceptionHandler(MediaNotFoundException.class)
    public ResponseEntity<StandardException> mediaNotFound(MediaNotFoundException exception, HttpServletRequest request) {
        int statusCode = HttpStatus.NOT_FOUND.value();
        StandardException responseException = new StandardException(
                exception.getMessage(),
                Instant.now(),
                "Media not found",
                request.getRequestURI(),
                statusCode);
        return ResponseEntity.status(statusCode).body(responseException);
    }


    /**
     * Handles database communication exceptions.
     * Returns a 502 BAD_GATEWAY status with detailed error information.
     * @param exception The thrown DatabaseException
     * @param request The HTTP request that triggered the exception
     * @return ResponseEntity containing standardized error details
     */
    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<StandardException> databaseException(DatabaseException exception, HttpServletRequest request) {
        int statusCode = HttpStatus.BAD_GATEWAY.value();
        StandardException responseException = new StandardException(
                exception.getMessage(),
                Instant.now(),
                "Error with database communication.",
                request.getRequestURI(),
                statusCode);
        return ResponseEntity.status(statusCode).body(responseException);
    }
}
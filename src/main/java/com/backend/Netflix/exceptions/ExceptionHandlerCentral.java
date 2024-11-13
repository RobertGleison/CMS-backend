package com.backend.Netflix.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class ExceptionHandlerCentral {

    @ExceptionHandler(MediaNotFoundException.class)
    public ResponseEntity<StandardException> mediaNotFound(MediaNotFoundException exception, HttpServletRequest request){
        int statusCode = HttpStatus.NOT_FOUND.value();
        StandardException responseException = new StandardException(
                exception.getMessage(),
                Instant.now(),
                "Media not found",
                request.getRequestURI(),
                statusCode);
        return ResponseEntity.status(statusCode).body(responseException);
    }

    @ExceptionHandler(DBInsertException.class)
    public ResponseEntity<StandardException> insertFailed(DBInsertException exception, HttpServletRequest request){
        int statusCode = HttpStatus.BAD_REQUEST.value();
        StandardException responseException = new StandardException(
                exception.getMessage(),
                Instant.now(),
                "Media not found",
                request.getRequestURI(),
                statusCode);
        return ResponseEntity.status(statusCode).body(responseException);
    }

    @ExceptionHandler(DatabaseAccessException.class)
    public ResponseEntity<StandardException> insertFailed(DatabaseAccessException exception, HttpServletRequest request){
        int statusCode = HttpStatus.BAD_REQUEST.value();
        StandardException responseException = new StandardException(
                exception.getMessage(),
                Instant.now(),
                "Database Error",
                request.getRequestURI(),
                statusCode);
        return ResponseEntity.status(statusCode).body(responseException);
    }
}

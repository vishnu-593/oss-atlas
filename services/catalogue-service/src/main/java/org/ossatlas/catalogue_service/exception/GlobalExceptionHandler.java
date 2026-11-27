
package org.ossatlas.catalogue_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle request validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException exception) {

        Map<String, String> validationErrors = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error ->
                validationErrors.put(
                        error.getField(),
                        error.getDefaultMessage()
                )
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("details", validationErrors);

        return ResponseEntity.badRequest().body(response);
    }

    // Handle unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralErrors(
            Exception exception) {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", Instant.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Internal Server Error");
        response.put("message", "An unexpected error occurred");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

@ExceptionHandler(DataIntegrityViolationException.class)
public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(
        DataIntegrityViolationException ex) {

    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", Instant.now());
    body.put("status", HttpStatus.CONFLICT.value());
    body.put("error", "Conflict");
    body.put("message", "A project with this GitHub URL already exists");

    return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
}

@ExceptionHandler(ResponseStatusException.class)
public ResponseEntity<Map<String, Object>> handleResponseStatusException(
        ResponseStatusException ex) {

    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", Instant.now());
    body.put("status", ex.getStatusCode().value());
    body.put("error", ex.getStatusCode().toString());
    body.put("message", ex.getReason());

    return new ResponseEntity<>(body, ex.getStatusCode());
}
}

package com.shotaroi.featureflags.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse illegalArg(IllegalArgumentException e) {
        return ErrorResponse.of(400, "BAD_REQUEST", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse validation(MethodArgumentNotValidException e) {
        return ErrorResponse.of(400, "VALIDATION_ERROR", "Request validation failed");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse badJson(HttpMessageNotReadableException e) {
        return ErrorResponse.of(400, "BAD_JSON", "Malformed JSON or wrong field types");
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse notFound(Exception e) {
        return ErrorResponse.of(404, "NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse fallback(Exception e) {
        return ErrorResponse.of(500, "INTERNAL_ERROR", e.getClass().getSimpleName() + ": " + e.getMessage());
    }

    public record ErrorResponse(Instant timestamp, int status, String error, String message) {
        static ErrorResponse of(int status, String error, String message) {
            return new ErrorResponse(Instant.now(), status, error, message);
        }
    }
}

package com.servicehubai.common.api;

import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.servicehubai.common.exception.ApiException;
import com.servicehubai.config.CorrelationIdFilter;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiError> handleApiException(ApiException exception, HttpServletRequest request) {
        return response(exception.getStatus(), exception.getTitle(), exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<ApiError.FieldError> fields = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiError.FieldError(error.getField(), error.getDefaultMessage()))
                .toList();
        return response(HttpStatus.BAD_REQUEST, "Validation failed", "One or more fields are invalid.", request, fields);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "Validation failed", "One or more fields are invalid.", request, List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException exception, HttpServletRequest request) {
        return response(HttpStatus.FORBIDDEN, "Forbidden", "You do not have permission to perform this action.", request, List.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpected(Exception exception, HttpServletRequest request) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", "The request could not be completed.", request, List.of());
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String title, String detail,
            HttpServletRequest request, List<ApiError.FieldError> fields) {
        Object requestCorrelationId = request.getAttribute(CorrelationIdFilter.ATTRIBUTE_NAME);
        String correlationId = requestCorrelationId == null
            ? UUID.randomUUID().toString()
            : requestCorrelationId.toString();
        ApiError error = new ApiError(
                "https://servicehub.ai/errors/" + status.value(),
                title,
                status.value(),
                detail,
                request.getRequestURI(),
                correlationId,
                fields);
        return ResponseEntity.status(status)
            .header(CorrelationIdFilter.HEADER_NAME, correlationId)
            .body(error);
    }
}

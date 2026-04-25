package com.smart_campus_hub.smart_campus_api.exception;

import com.smart_campus_hub.smart_campus_api.dto.auth.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        FieldError firstError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String message = firstError != null ? firstError.getDefaultMessage() : "Validation failed.";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBadBody(HttpMessageNotReadableException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("Request payload is invalid."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        String msg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        String normalized = msg == null ? "" : msg.toLowerCase();

        if (normalized.contains("fk_bookings_resource") ||
                (normalized.contains("foreign key") && normalized.contains("resource"))) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("Selected resource is invalid or unavailable for bookings."));
        }

        if (normalized.contains("fk_bookings_user") ||
                (normalized.contains("foreign key") && normalized.contains("user"))) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("Booking user is invalid. Please sign in again and retry."));
        }
        
        if (normalized.contains("duplicate") || normalized.contains("unique")) {
            if (normalized.contains("user") || normalized.contains("email") || normalized.contains("username")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Duplicate username or email."));
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("A record with this value already exists."));
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Database constraint violation."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
    }
}

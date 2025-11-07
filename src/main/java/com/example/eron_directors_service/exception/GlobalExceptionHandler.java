package com.example.eron_directors_service.exception;
import com.example.eron_directors_service.dto.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        String message = ex.getConstraintViolations().stream().map(jakarta.validation.ConstraintViolation::getMessage).findFirst().orElse("Invalid request.");
        return respond(HttpStatus.BAD_REQUEST, message, request);
    }
    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorResponse> handleExternalApiException(ExternalApiException ex, WebRequest request) {
        return respond(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", request);
    }
    private ResponseEntity<ErrorResponse> respond(HttpStatus status, String message, WebRequest request) {
        String path = request != null ? request.getDescription(false).replaceFirst("uri=", "") : "/";
        return ResponseEntity.status(status.value()).body(ErrorResponse.builder().timestamp(OffsetDateTime.now().toString()).status(status.value()).error(status.getReasonPhrase()).message(message).path(path).build());
    }
}


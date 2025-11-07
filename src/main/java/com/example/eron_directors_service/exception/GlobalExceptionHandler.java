package com.example.eron_directors_service.exception;
import com.example.eron_directors_service.dto.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, ServerWebExchange exchange) {
        String message = ex.getConstraintViolations().stream().map(jakarta.validation.ConstraintViolation::getMessage).findFirst().orElse("Invalid request.");
        return respond(HttpStatus.BAD_REQUEST, message, exchange);
    }
    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ErrorResponse> handleServerWebInputException(ServerWebInputException ex, ServerWebExchange exchange) {
        String message = ex.getReason() != null ? ex.getReason() : "Invalid request.";
        return respond(HttpStatus.BAD_REQUEST, message, exchange);
    }
    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorResponse> handleExternalApiException(ExternalApiException ex, ServerWebExchange exchange) {
        return respond(HttpStatus.BAD_GATEWAY, ex.getMessage(), exchange);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, ServerWebExchange exchange) {
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", exchange);
    }
    private ResponseEntity<ErrorResponse> respond(HttpStatus status, String message, ServerWebExchange exchange) {
        String path = exchange != null ? exchange.getRequest().getPath().value() : "/";
        return ResponseEntity.status(status.value()).body(ErrorResponse.builder().timestamp(OffsetDateTime.now().toString()).status(status.value()).error(status.getReasonPhrase()).message(message).path(path).build());
    }
}


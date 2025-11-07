package com.example.movie_directors_service.exception;

import com.example.movie_directors_service.dto.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

@RestControllerAdvice
@ConditionalOnWebApplication(type = Type.REACTIVE)
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            ServerWebExchange exchange) {
        String message = ex.getConstraintViolations().stream()
                .map(jakarta.validation.ConstraintViolation::getMessage)
                .findFirst()
                .orElse("Invalid request.");
        return respond(HttpStatus.BAD_REQUEST, message, exchange);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            Exception ex,
            ServerWebExchange exchange) {
        String message = "Invalid request.";

        if (ex instanceof MethodArgumentNotValidException manve) {
            message = manve.getBindingResult().getFieldErrors().stream()
                    .map(error -> String.format("%s %s", error.getField(), error.getDefaultMessage()))
                    .findFirst()
                    .orElse(message);
        } else if (ex instanceof BindException bindException) {
            message = bindException.getBindingResult().getFieldErrors().stream()
                    .map(error -> String.format("%s %s", error.getField(), error.getDefaultMessage()))
                    .findFirst()
                    .orElse(message);
        }

        return respond(HttpStatus.BAD_REQUEST, message, exchange);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            ServerWebExchange exchange) {
        String parameterName = ex.getName();
        String errorMessage = String.format("Parameter '%s' must be a non-negative integer", parameterName);
        return respond(HttpStatus.BAD_REQUEST, errorMessage, exchange);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            ServerWebExchange exchange) {
        String errorMessage = String.format("Missing required parameter '%s'", ex.getParameterName());
        return respond(HttpStatus.BAD_REQUEST, errorMessage, exchange);
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ErrorResponse> handleServerWebInputException(
            ServerWebInputException ex,
            ServerWebExchange exchange) {
        return respond(HttpStatus.BAD_REQUEST, "Threshold must be a non-negative integer", exchange);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex,
            ServerWebExchange exchange) {
        String message = ex.getAllErrors().stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("Invalid request.");

        String path = Optional.ofNullable(exchange)
                .map(serverWebExchange -> serverWebExchange.getRequest().getPath().value())
                .orElse("/");

        return respond(HttpStatus.BAD_REQUEST, message, path);
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorResponse> handleExternalApiException(
            ExternalApiException ex,
            ServerWebExchange exchange) {
        return respond(HttpStatus.BAD_GATEWAY, ex.getMessage(), exchange);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            ServerWebExchange exchange) {
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", exchange);
    }

    private ResponseEntity<ErrorResponse> respond(HttpStatus status, String message, ServerWebExchange exchange) {
        String path = Optional.ofNullable(exchange)
                .map(serverWebExchange -> serverWebExchange.getRequest().getPath().value())
                .orElse("/");
        return respond(status, message, path);
    }

    private ResponseEntity<ErrorResponse> respond(HttpStatus status, String message, String path) {
        return ResponseEntity.status(status.value())
                .body(ErrorResponse.builder()
                        .timestamp(OffsetDateTime.now().toString())
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .message(message)
                        .path(path)
                        .build());
    }
}


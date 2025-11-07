package com.example.eron_directors_service.exception;

import com.example.eron_directors_service.dto.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.Objects;
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
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ServerWebInputException;

@RestControllerAdvice
@ConditionalOnWebApplication(type = Type.SERVLET)
public class ServletGlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            WebRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(jakarta.validation.ConstraintViolation::getMessage)
                .findFirst()
                .orElse("Invalid request.");
        return respond(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            Exception ex,
            WebRequest request) {
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

        return respond(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            WebRequest request) {
        String parameterName = ex.getName();
        String errorMessage = String.format("Parameter '%s' must be a non-negative integer", parameterName);
        return respond(HttpStatus.BAD_REQUEST, errorMessage, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            WebRequest request) {
        String errorMessage = String.format("Missing required parameter '%s'", ex.getParameterName());
        return respond(HttpStatus.BAD_REQUEST, errorMessage, request);
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ErrorResponse> handleServerWebInputException(
            ServerWebInputException ex,
            WebRequest request) {
        return respond(HttpStatus.BAD_REQUEST, "Threshold must be a non-negative integer", request);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex,
            WebRequest request) {
        String message = ex.getAllErrors().stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("Invalid request.");

        return respond(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorResponse> handleExternalApiException(
            ExternalApiException ex,
            WebRequest request) {
        return respond(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request) {
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", request);
    }

    private ResponseEntity<ErrorResponse> respond(HttpStatus status, String message, WebRequest request) {
        String path = request != null ? extractPath(request) : "/";
        return ResponseEntity.status(status.value())
                .body(ErrorResponse.builder()
                        .timestamp(OffsetDateTime.now().toString())
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .message(message)
                        .path(path)
                        .build());
    }

    private String extractPath(WebRequest request) {
        String description = request.getDescription(false); // e.g. "uri=/api/directors"
        if (description != null && description.startsWith("uri=")) {
            return description.substring(4);
        }
        return Objects.requireNonNullElse(description, "/");
    }
}


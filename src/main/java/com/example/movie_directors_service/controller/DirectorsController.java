package com.example.movie_directors_service.controller;

import com.example.movie_directors_service.dto.response.ErrorResponse;
import com.example.movie_directors_service.exception.InvalidParameterException;
import com.example.movie_directors_service.service.DirectorsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/directors")
public class DirectorsController {

    private final DirectorsService directorsService;

    public DirectorsController(DirectorsService directorsService) {
        this.directorsService = directorsService;
    }

    @GetMapping
    public Mono<ResponseEntity<Object>> getDirectorsAboveThreshold(
            @RequestParam("threshold") String thresholdParam) {

        try {
            long threshold = parseAndValidateThreshold(thresholdParam);

            return directorsService.getDirectorsAboveThreshold(threshold)
                    .map(directors -> ResponseEntity.ok().body((Object) directors));
        } catch (InvalidParameterException ex) {
            return Mono.just(ResponseEntity.badRequest().body(buildErrorResponse(ex.getMessage())));
        }
    }

    private long parseAndValidateThreshold(String thresholdParam) {
        if (thresholdParam == null || thresholdParam.trim().isEmpty()) {
            throw new InvalidParameterException("Parameter 'threshold' cannot be empty or blank");
        }

        try {
            long threshold = Long.parseLong(thresholdParam.trim());
            if (threshold < 0) {
                throw new InvalidParameterException("Threshold must be non-negative");
            }
            return threshold;
        } catch (NumberFormatException e) {
            throw new InvalidParameterException("Invalid value for parameter 'threshold': '" + thresholdParam + "' must be a valid number", e);
        }
    }

    private ErrorResponse buildErrorResponse(String message) {
        return ErrorResponse.builder()
                .timestamp(OffsetDateTime.now().toString())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(message)
                .path("/api/directors")
                .build();
    }
}
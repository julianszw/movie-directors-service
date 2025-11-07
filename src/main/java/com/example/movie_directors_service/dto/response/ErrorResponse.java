package com.example.movie_directors_service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

/**
 * Immutable DTO that captures standard error details returned by the API.
 */
@Getter
@Builder
@Jacksonized
public class ErrorResponse {

    @JsonProperty("timestamp")
    private final String timestamp;

    @JsonProperty("status")
    private final int status;

    @JsonProperty("error")
    private final String error;

    @JsonProperty("message")
    private final String message;

    @JsonProperty("path")
    private final String path;
}

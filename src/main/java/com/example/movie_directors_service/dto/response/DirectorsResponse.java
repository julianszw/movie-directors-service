package com.example.movie_directors_service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * Immutable DTO that encapsulates the list of directors returned by the service.
 */
@Getter
@Builder
@Jacksonized
@EqualsAndHashCode
public class DirectorsResponse {

    @JsonProperty("directors")
    private final List<String> directors;

    private DirectorsResponse(List<String> directors) {
        this.directors = directors == null ? List.of() : List.copyOf(directors);
    }

    public static DirectorsResponse of(List<String> directors) {
        return new DirectorsResponse(directors);
    }
}

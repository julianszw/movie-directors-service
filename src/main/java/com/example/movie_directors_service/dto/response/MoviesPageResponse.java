package com.example.movie_directors_service.dto.response;

import com.example.movie_directors_service.model.Movie;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * Immutable DTO that captures one page of movies as returned by the external API.
 */
@Getter
@Builder
@Jacksonized
public class MoviesPageResponse {

    @JsonProperty("page")
    private final int page;

    @JsonProperty("per_page")
    private final int perPage;

    @JsonProperty("total")
    private final int total;

    @JsonProperty("total_pages")
    private final int totalPages;

    @JsonProperty("data")
    private final List<Movie> data;

    private MoviesPageResponse(int page, int perPage, int total, int totalPages, List<Movie> data) {
        this.page = page;
        this.perPage = perPage;
        this.total = total;
        this.totalPages = totalPages;
        this.data = data == null ? List.of() : List.copyOf(data);
    }
}

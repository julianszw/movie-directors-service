package com.example.movie_directors_service.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Immutable domain model that represents a movie as retrieved from the external Eron movies API.
 * <p>
 * Example usage:
 * {@code Movie movie = Movie.builder().title("Shutter Island").year(2010).rated("R").released("19 Feb 2010").runtime("138 min")
 * .genre("Mystery, Thriller").director("Martin Scorsese").writer("Laeta Kalogridis").actors("Leonardo DiCaprio, Mark Ruffalo").build();}
 */
@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ = @JsonCreator)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Movie {

    @JsonProperty("Title")
    private final String title;

    @JsonProperty("Year")
    private final int year;

    @JsonProperty("Rated")
    private final String rated;

    @JsonProperty("Released")
    private final String released;

    @JsonProperty("Runtime")
    private final String runtime;

    @JsonProperty("Genre")
    private final String genre;

    @JsonProperty("Director")
    private final String director;

    @JsonProperty("Writer")
    private final String writer;

    @JsonProperty("Actors")
    private final String actors;

    public boolean hasValidDirector() {
        return director != null && !director.isBlank();
    }
}


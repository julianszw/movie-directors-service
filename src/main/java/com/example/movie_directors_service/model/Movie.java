package com.example.movie_directors_service.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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

    @NotBlank(message = "Title must not be blank")
    @JsonProperty("Title")
    private final String title;

    @Min(value = 1900, message = "Year must not be earlier than 1900")
    @Max(value = 2100, message = "Year must not be in the far future")
    @JsonProperty("Year")
    private final int year;

    @NotBlank(message = "Rated value must not be blank")
    @JsonProperty("Rated")
    private final String rated;

    @NotBlank(message = "Released date must not be blank")
    @JsonProperty("Released")
    private final String released;

    @NotBlank(message = "Runtime must not be blank")
    @JsonProperty("Runtime")
    private final String runtime;

    @NotEmpty(message = "Genre list must not be empty")
    @JsonProperty("Genre")
    private final String genre;

    @NotBlank(message = "Director must not be blank")
    @JsonProperty("Director")
    private final String director;

    @NotBlank(message = "Writer information must not be blank")
    @JsonProperty("Writer")
    private final String writer;

    @NotBlank(message = "Actors information must not be blank")
    @JsonProperty("Actors")
    private final String actors;

    public boolean hasValidDirector() {
        return director != null && !director.isBlank();
    }
}


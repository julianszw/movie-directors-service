package com.example.eron_directors_service.client;

import com.example.eron_directors_service.dto.response.MoviesPageResponse;
import com.example.eron_directors_service.exception.ExternalApiException;
import com.example.eron_directors_service.model.Movie;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoviesApiClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void fetchMoviesPage_timesOutAndWrapsInExternalApiException() {
        ExchangeFunction exchangeFunction = request -> Mono.never();
        MoviesApiClient client = new MoviesApiClient(
                WebClient.builder().exchangeFunction(exchangeFunction),
                "http://localhost",
                0,
                0,
                0);

        StepVerifier.create(client.fetchMoviesPage(7))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(ExternalApiException.class, error);
                    assertTrue(error.getMessage().contains("timeout or network error"));
                })
                .verify(Duration.ofSeconds(1));
    }

    @Test
    void fetchMoviesPage_retriesOnServerErrorsAndEventuallySucceeds() {
        AtomicInteger attempts = new AtomicInteger();

        ExchangeFunction exchangeFunction = request -> {
            int currentAttempt = attempts.incrementAndGet();
            if (currentAttempt <= 2) {
                return Mono.just(serverErrorResponse());
            }
            return Mono.just(successResponse());
        };

        MoviesApiClient client = new MoviesApiClient(
                WebClient.builder().exchangeFunction(exchangeFunction),
                "http://localhost",
                30,
                2,
                0);

        StepVerifier.create(client.fetchMoviesPage(3))
                .expectNextMatches(response -> response.getPage() == 1 && response.getData().size() == 1)
                .verifyComplete();

        assertEquals(3, attempts.get());
    }

    @Test
    void fetchMoviesPage_mapsClientErrorsToExternalApiExceptionWithoutRetries() {
        AtomicInteger attempts = new AtomicInteger();

        ExchangeFunction exchangeFunction = request -> {
            attempts.incrementAndGet();
            return Mono.just(clientErrorResponse());
        };

        MoviesApiClient client = new MoviesApiClient(
                WebClient.builder().exchangeFunction(exchangeFunction),
                "http://localhost",
                30,
                2,
                0);

        StepVerifier.create(client.fetchMoviesPage(5))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(ExternalApiException.class, error);
                    assertTrue(error.getMessage().contains("Status: 404 NOT_FOUND"));
                })
                .verify(Duration.ofSeconds(1));

        assertEquals(1, attempts.get());
    }

    @Test
    void fetchMoviesPage_mapsPersistentServerErrorsToExternalApiExceptionAfterRetries() {
        AtomicInteger attempts = new AtomicInteger();

        ExchangeFunction exchangeFunction = request -> {
            attempts.incrementAndGet();
            return Mono.just(serverErrorResponse());
        };

        MoviesApiClient client = new MoviesApiClient(
                WebClient.builder().exchangeFunction(exchangeFunction),
                "http://localhost",
                30,
                2,
                0);

        StepVerifier.create(client.fetchMoviesPage(9))
                .expectErrorSatisfies(error -> {
                    ExternalApiException external = assertInstanceOf(ExternalApiException.class, error);
                    assertTrue(external.getMessage().contains("timeout or network error"));
                    Throwable cause = external.getCause();
                    assertTrue(cause instanceof RuntimeException);
                    assertTrue(cause.getClass().getSimpleName().contains("RetryExhaustedException"));
                    Throwable exhaustedCause = cause.getCause();
                    assertInstanceOf(WebClientResponseException.class, exhaustedCause);
                })
                .verify(Duration.ofSeconds(1));

        assertEquals(3, attempts.get());
    }

    private ClientResponse successResponse() {
        MoviesPageResponse payload = MoviesPageResponse.builder()
                .page(1)
                .perPage(1)
                .total(1)
                .totalPages(1)
                .data(List.of(sampleMovie("Director Success")))
                .build();

        return ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(toJson(payload))
                .build();
    }

    private ClientResponse serverErrorResponse() {
        return ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(toJson(Map.of("error", "Server error")))
                .build();
    }

    private ClientResponse clientErrorResponse() {
        return ClientResponse.create(HttpStatus.NOT_FOUND)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(toJson(Map.of("error", "Not found")))
                .build();
    }

    private Movie sampleMovie(String director) {
        return Movie.builder()
                .title("Sample")
                .year(2020)
                .rated("PG-13")
                .released("2020-01-01")
                .runtime("120 min")
                .genre("Drama")
                .director(director)
                .writer("Writer")
                .actors("Actor")
                .build();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize body for test setup", ex);
        }
    }
}



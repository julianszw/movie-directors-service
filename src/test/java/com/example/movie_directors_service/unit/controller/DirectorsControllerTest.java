package com.example.movie_directors_service.unit.controller;

import com.example.movie_directors_service.controller.DirectorsController;
import com.example.movie_directors_service.dto.response.DirectorsResponse;
import com.example.movie_directors_service.exception.GlobalExceptionHandler;
import com.example.movie_directors_service.service.DirectorsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = DirectorsController.class)
@Import(GlobalExceptionHandler.class)
class DirectorsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private DirectorsService directorsService;

    @BeforeEach
    void resetMocks() {
        reset(directorsService);
    }

    @Test
    void givenPositiveThreshold_whenRequestingDirectors_thenReturnsDirectorsList() {
        when(directorsService.getDirectorsAboveThreshold(3L))
                .thenReturn(Mono.just(DirectorsResponse.of(List.of("Director A", "Director B"))));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/directors")
                        .queryParam("threshold", "3")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.directors[0]").isEqualTo("Director A")
                .jsonPath("$.directors[1]").isEqualTo("Director B");

        verify(directorsService).getDirectorsAboveThreshold(3L);
    }

    @Test
    void givenNonNumericThreshold_whenRequestingDirectors_thenReturnsBadRequest() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/directors")
                        .queryParam("threshold", "invalid")
                        .build())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").value(message ->
                        message.toString().contains("must be a valid number"));

        verifyNoInteractions(directorsService);
    }

    @Test
    void givenZeroThreshold_whenRequestingDirectors_thenReturnsDirectorsList() {
        when(directorsService.getDirectorsAboveThreshold(0L))
                .thenReturn(Mono.just(DirectorsResponse.of(List.of("Director Zero"))));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/directors")
                        .queryParam("threshold", "0")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.directors[0]").isEqualTo("Director Zero");

        verify(directorsService).getDirectorsAboveThreshold(0L);
    }

    @Test
    void givenNegativeThreshold_whenRequestingDirectors_thenReturnsBadRequest() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/directors")
                        .queryParam("threshold", "-5")
                        .build())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").value(message ->
                        message.toString().contains("non-negative"));

        verifyNoInteractions(directorsService);
    }
}
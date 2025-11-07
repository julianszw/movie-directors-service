package com.example.movie_directors_service.client;

import com.example.movie_directors_service.dto.response.MoviesPageResponse;
import com.example.movie_directors_service.exception.ExternalApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class MoviesApiClient {
    
    private final WebClient webClient;
    private final Duration timeoutDuration;
    private final int maxRetries;
    private final Duration retryDelay;
    
    public MoviesApiClient(
            WebClient.Builder webClientBuilder,
            @Value("${movies.api.base-url:https://wiremock.dev.eroninternational.com}") String baseUrl,
            @Value("${movies.api.timeout-seconds:30}") long timeoutSeconds,
            @Value("${movies.api.max-retries:2}") int maxRetries,
            @Value("${movies.api.retry-delay-seconds:1}") long retryDelaySeconds) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl != null ? baseUrl : "https://wiremock.dev.eroninternational.com")
                .build();
        this.timeoutDuration = Duration.ofSeconds(timeoutSeconds);
        this.maxRetries = maxRetries;
        this.retryDelay = Duration.ofSeconds(retryDelaySeconds);
    }
    
    public Mono<MoviesPageResponse> fetchMoviesPage(int page) {
        return webClient
                .get()
                .uri("/api/movies/search?page={page}", page)
                .retrieve()
                .bodyToMono(MoviesPageResponse.class)
                .timeout(timeoutDuration)
                .retryWhen(Retry.backoff(maxRetries, retryDelay)
                        .filter(throwable -> throwable instanceof WebClientResponseException 
                                && ((WebClientResponseException) throwable).getStatusCode().is5xxServerError()))
                .onErrorMap(WebClientResponseException.class, ex -> 
                    new ExternalApiException(
                        String.format("Failed to fetch movies from page %d. Status: %s", 
                                page, ex.getStatusCode()), ex))
                .onErrorMap(throwable -> !(throwable instanceof ExternalApiException), ex -> 
                    new ExternalApiException(
                        String.format("Failed to fetch movies from page %d due to timeout or network error", page), ex));
    }

    public Mono<Boolean> isApiHealthy() {
        return webClient
                .get()
                .uri("/api/movies/search?page={page}", 1)
                .exchangeToMono(response -> response.releaseBody()
                        .thenReturn(response.statusCode().is2xxSuccessful()))
                .timeout(Duration.ofSeconds(5))
                .onErrorReturn(false);
    }
}


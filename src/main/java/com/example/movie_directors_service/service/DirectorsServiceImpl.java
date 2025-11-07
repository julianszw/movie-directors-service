package com.example.movie_directors_service.service;

import com.example.movie_directors_service.client.MoviesApiClient;
import com.example.movie_directors_service.dto.response.DirectorsResponse;
import com.example.movie_directors_service.dto.response.MoviesPageResponse;
import com.example.movie_directors_service.model.Movie;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * WebFlux-backed service that keeps the entire processing pipeline non-blocking to sustain
 * throughput in high-volume environments.
 */
@Service
public class DirectorsServiceImpl implements DirectorsService {

    private static final int PAGE_FETCH_CONCURRENCY = 8;

    private final MoviesApiClient moviesApiClient;

    public DirectorsServiceImpl(MoviesApiClient moviesApiClient) {
        this.moviesApiClient = moviesApiClient;
    }

    @Override
    public Mono<DirectorsResponse> getDirectorsAboveThreshold(Long threshold) {
        return fetchAllMoviePages()
                .flatMapIterable(MoviesPageResponse::getData)
                .filter(Movie::hasValidDirector)
                .groupBy(Movie::getDirector)
                .flatMap(group -> group.count()
                        .filter(count -> count > threshold)
                        .map(count -> group.key()))
                .sort()
                .collectList()
                .map(directors -> DirectorsResponse.builder()
                        .directors(directors)
                        .build());
    }

    private Flux<MoviesPageResponse> fetchAllMoviePages() {
        return moviesApiClient.fetchMoviesPage(1)
                .flatMapMany(firstPage -> {
                    int totalPages = Math.max(firstPage.getTotalPages(), 1);

                    return Flux.range(1, totalPages)
                            .flatMap(page -> page == 1
                                            ? Mono.just(firstPage)
                                            : moviesApiClient.fetchMoviesPage(page)
                                                    .onErrorResume(error -> Mono.empty()),
                                    PAGE_FETCH_CONCURRENCY);
                })
                .onErrorResume(error -> Flux.empty());
    }
}

package com.example.movie_directors_service.service;

import com.example.movie_directors_service.client.MoviesApiClient;
import com.example.movie_directors_service.dto.response.DirectorsResponse;
import com.example.movie_directors_service.dto.response.MoviesPageResponse;
import com.example.movie_directors_service.model.Movie;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DirectorsServiceImpl implements DirectorsService {

    private final MoviesApiClient moviesApiClient;

    public DirectorsServiceImpl(MoviesApiClient moviesApiClient) {
        this.moviesApiClient = moviesApiClient;
    }

    @Override
    public Mono<DirectorsResponse> getDirectorsAboveThreshold(Long threshold) {
        return fetchAllMovies()
                .map(movies -> {
                    Map<String, Long> directorCounts = countMoviesByDirector(movies);
                    List<String> filteredDirectors = filterDirectorsAboveThreshold(directorCounts, threshold);
                    return DirectorsResponse.of(filteredDirectors);
                });
    }

    private Mono<List<Movie>> fetchAllMovies() {
        return moviesApiClient.fetchMoviesPage(1)
                .flatMap(firstPage -> {
                    int totalPages = firstPage.getTotalPages();
                    List<Movie> allMovies = new ArrayList<>(firstPage.getData() != null ? firstPage.getData() : List.of());

                    if (totalPages <= 1) {
                        return Mono.just(allMovies);
                    }

                    Flux<MoviesPageResponse> remainingPages = Flux.range(2, totalPages - 1)
                            .flatMap(page -> moviesApiClient.fetchMoviesPage(page)
                                            .onErrorResume(e -> Mono.empty()),  // ← SOLO ESTA LÍNEA NUEVA
                                    5);

                    return remainingPages
                            .map(MoviesPageResponse::getData)
                            .collectList()
                            .map(pageDataList -> {
                                for (List<Movie> pageData : pageDataList) {
                                    if (pageData != null) {
                                        allMovies.addAll(pageData);
                                    }
                                }
                                return allMovies;
                            });
                })
                .onErrorReturn(List.of());
    }

    private Map<String, Long> countMoviesByDirector(List<Movie> movies) {
        return movies.stream()
                .filter(Movie::hasValidDirector)
                .collect(Collectors.groupingBy(
                        Movie::getDirector,
                        Collectors.counting()
                ));
    }

    private List<String> filterDirectorsAboveThreshold(Map<String, Long> directorCounts, Long threshold) {
        return directorCounts.entrySet().stream()
                .filter(entry -> entry.getValue() > threshold)
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());
    }
}
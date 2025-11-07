package com.example.movie_directors_service.service;

import com.example.movie_directors_service.dto.response.DirectorsResponse;
import reactor.core.publisher.Mono;

/**
 * Service contract for retrieving directors information.
 */
public interface DirectorsService {

    /**
     * Retrieves directors whose number of movies is strictly greater than the provided threshold.
     *
     * @param threshold threshold to evaluate directors against (must be non-negative)
     * @return reactive stream of alphabetically sorted director names
     */
    Mono<DirectorsResponse> getDirectorsAboveThreshold(Long threshold);
}


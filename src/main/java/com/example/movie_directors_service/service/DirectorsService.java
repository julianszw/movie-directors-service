package com.example.movie_directors_service.service;

import com.example.movie_directors_service.dto.response.DirectorsResponse;
import reactor.core.publisher.Mono;

public interface DirectorsService {
    Mono<DirectorsResponse> getDirectorsAboveThreshold(Long threshold);
}
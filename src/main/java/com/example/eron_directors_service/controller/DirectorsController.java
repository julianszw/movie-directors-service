package com.example.eron_directors_service.controller;

import com.example.eron_directors_service.client.MoviesApiClient;
import com.example.eron_directors_service.dto.response.DirectorsResponse;
import com.example.eron_directors_service.service.DirectorsService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/**
 * REST controller leveraging WebFlux to deliver non-blocking, scalable access to director insights.
 */
@RestController
@Validated
@RequestMapping("/api/directors")
public class DirectorsController {
    
    private final DirectorsService directorsService;
    private final MoviesApiClient moviesApiClient;
    
    public DirectorsController(DirectorsService directorsService, MoviesApiClient moviesApiClient) {
        this.directorsService = directorsService;
        this.moviesApiClient = moviesApiClient;
    }
    
    @GetMapping
    public Mono<DirectorsResponse> getDirectorsAboveThreshold(
            @RequestParam("threshold") @NotNull @Min(value = 0, message = "Threshold must be a non-negative integer") Integer threshold) {

        return moviesApiClient.isApiHealthy()
                .flatMap(isHealthy -> {
                    if (!isHealthy) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "The movies service is currently unavailable. Please try again later."));
                    }
                    return directorsService.getDirectorsAboveThreshold(threshold);
                });
    }
}


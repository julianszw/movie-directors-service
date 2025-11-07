package com.example.movie_directors_service.controller;

import com.example.movie_directors_service.dto.response.DirectorsResponse;
import com.example.movie_directors_service.service.DirectorsService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * REST controller leveraging WebFlux to deliver non-blocking, scalable access to director insights.
 */
@RestController
@Validated
@RequestMapping("/api/directors")
public class DirectorsController {
    
    private final DirectorsService directorsService;
    
    public DirectorsController(DirectorsService directorsService) {
        this.directorsService = directorsService;
    }
    
    @GetMapping
    public Mono<DirectorsResponse> getDirectorsAboveThreshold(
            @RequestParam("threshold") @NotNull @Min(value = 0, message = "Threshold must be a non-negative integer") Long threshold) {
        return directorsService.getDirectorsAboveThreshold(threshold);
    }
}


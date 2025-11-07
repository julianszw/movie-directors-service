package com.example.eron_directors_service.unit.service;

import com.example.eron_directors_service.client.MoviesApiClient;
import com.example.eron_directors_service.dto.response.DirectorsResponse;
import com.example.eron_directors_service.dto.response.MoviesPageResponse;
import com.example.eron_directors_service.model.Movie;
import com.example.eron_directors_service.service.DirectorsService;
import com.example.eron_directors_service.service.DirectorsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DirectorsServiceTest {

    @Mock
    private MoviesApiClient moviesApiClient;

    private DirectorsService directorsService;

    @BeforeEach
    void setUp() {
        directorsService = new DirectorsServiceImpl(moviesApiClient);
    }

    @Test
    void testGetDirectorsAboveThreshold_SinglePage() {
        Movie movie1 = createMovieWithDirector("Martin Scorsese");
        Movie movie2 = createMovieWithDirector("Woody Allen");
        Movie movie3 = createMovieWithDirector("Martin Scorsese");
        Movie movie4 = createMovieWithDirector("Woody Allen");
        
        MoviesPageResponse page1 = createMoviesPageResponse(
                Arrays.asList(movie1, movie2, movie3, movie4),
                1,
                1);

        when(moviesApiClient.fetchMoviesPage(1))
                .thenReturn(Mono.just(page1));

        StepVerifier.create(directorsService.getDirectorsAboveThreshold(1L))
                .expectNext(DirectorsResponse.of(List.of("Martin Scorsese", "Woody Allen")))
                .verifyComplete();

        verify(moviesApiClient, times(1)).fetchMoviesPage(1);
    }

    @Test
    void testGetDirectorsAboveThreshold_MultiplePages() {
        Movie movie1 = createMovieWithDirector("Martin Scorsese");
        Movie movie2 = createMovieWithDirector("Woody Allen");
        
        MoviesPageResponse page1 = createMoviesPageResponse(
                Arrays.asList(movie1, movie2),
                1,
                2);

        Movie movie3 = createMovieWithDirector("Martin Scorsese");
        Movie movie4 = createMovieWithDirector("Martin Scorsese");
        
        MoviesPageResponse page2 = createMoviesPageResponse(
                Arrays.asList(movie3, movie4),
                2,
                2);

        when(moviesApiClient.fetchMoviesPage(1))
                .thenReturn(Mono.just(page1));
        when(moviesApiClient.fetchMoviesPage(2))
                .thenReturn(Mono.just(page2));

        StepVerifier.create(directorsService.getDirectorsAboveThreshold(2L))
                .expectNext(DirectorsResponse.of(List.of("Martin Scorsese")))
                .verifyComplete();

        verify(moviesApiClient, times(1)).fetchMoviesPage(1);
        verify(moviesApiClient, times(1)).fetchMoviesPage(2);
    }

    @Test
    void testGetDirectorsAboveThreshold_FiltersNullAndBlankDirectors() {
        Movie movie1 = createMovieWithDirector("Martin Scorsese");
        Movie movie2 = createMovieWithDirector(null);  // Should be skipped
        Movie movie3 = createMovieWithDirector("");    // Should be skipped
        Movie movie4 = createMovieWithDirector("   "); // Should be skipped
        Movie movie5 = createMovieWithDirector("Woody Allen");
        Movie movie6 = createMovieWithDirector("Woody Allen");
        Movie movie7 = createMovieWithDirector("Martin Scorsese");
        
        MoviesPageResponse page1 = createMoviesPageResponse(
                Arrays.asList(movie1, movie2, movie3, movie4, movie5, movie6, movie7),
                1,
                1);

        when(moviesApiClient.fetchMoviesPage(1))
                .thenReturn(Mono.just(page1));

        StepVerifier.create(directorsService.getDirectorsAboveThreshold(1L))
                .expectNext(DirectorsResponse.of(List.of("Martin Scorsese", "Woody Allen")))
                .verifyComplete();
    }

    @Test
    void testGetDirectorsAboveThreshold_NoDirectorsAboveThreshold() {
        Movie movie1 = createMovieWithDirector("Director A");
        Movie movie2 = createMovieWithDirector("Director B");
        
        MoviesPageResponse page1 = createMoviesPageResponse(
                Arrays.asList(movie1, movie2),
                1,
                1);

        when(moviesApiClient.fetchMoviesPage(1))
                .thenReturn(Mono.just(page1));

        StepVerifier.create(directorsService.getDirectorsAboveThreshold(10L))
                .expectNext(DirectorsResponse.of(List.of()))
                .verifyComplete();
    }

    @Test
    void testGetDirectorsAboveThreshold_AlphabeticalOrder() {
        Movie movie1 = createMovieWithDirector("Zack Snyder");
        Movie movie2 = createMovieWithDirector("Martin Scorsese");
        Movie movie3 = createMovieWithDirector("Woody Allen");
        Movie movie4 = createMovieWithDirector("Zack Snyder");
        Movie movie5 = createMovieWithDirector("Martin Scorsese");
        Movie movie6 = createMovieWithDirector("Woody Allen");
        
        MoviesPageResponse page1 = createMoviesPageResponse(
                Arrays.asList(movie1, movie2, movie3, movie4, movie5, movie6),
                1,
                1);

        when(moviesApiClient.fetchMoviesPage(1))
                .thenReturn(Mono.just(page1));

        StepVerifier.create(directorsService.getDirectorsAboveThreshold(1L))
                .expectNext(DirectorsResponse.of(List.of("Martin Scorsese", "Woody Allen", "Zack Snyder")))
                .verifyComplete();
    }

    @Test
    void testGetDirectorsAboveThreshold_FirstPageFailureReturnsEmptyResponse() {
        when(moviesApiClient.fetchMoviesPage(1))
                .thenReturn(Mono.error(new RuntimeException("API unavailable")));

        StepVerifier.create(directorsService.getDirectorsAboveThreshold(1L))
                .expectNext(DirectorsResponse.of(List.of()))
                .verifyComplete();

        verify(moviesApiClient, times(1)).fetchMoviesPage(1);
    }

    @Test
    void testGetDirectorsAboveThreshold_ThresholdStrictlyGreaterThan() {
        Movie directorA = createMovieWithDirector("Director A");
        Movie directorB = createMovieWithDirector("Director B");

        MoviesPageResponse page = createMoviesPageResponse(
                Arrays.asList(directorA, directorA, directorB, directorB),
                1,
                1);

        when(moviesApiClient.fetchMoviesPage(1)).thenReturn(Mono.just(page));

        StepVerifier.create(directorsService.getDirectorsAboveThreshold(2L))
                .expectNext(DirectorsResponse.of(List.of()))
                .verifyComplete();
    }

    @Test
    void testGetDirectorsAboveThreshold_PartialPageFailuresAreIgnoredAndProcessingContinues() {
        Movie pageOneMovie = createMovieWithDirector("Director Alpha");
        Movie pageThreeMovie = createMovieWithDirector("Director Omega");

        MoviesPageResponse firstPage = createMoviesPageResponse(List.of(pageOneMovie), 1, 3);
        MoviesPageResponse thirdPage = createMoviesPageResponse(List.of(pageThreeMovie), 3, 3);

        TestPublisher<MoviesPageResponse> secondPagePublisher = TestPublisher.create();
        TestPublisher<MoviesPageResponse> thirdPagePublisher = TestPublisher.create();

        when(moviesApiClient.fetchMoviesPage(1)).thenReturn(Mono.just(firstPage));
        when(moviesApiClient.fetchMoviesPage(2)).thenReturn(secondPagePublisher.mono());
        when(moviesApiClient.fetchMoviesPage(3)).thenReturn(thirdPagePublisher.mono());

        StepVerifier.create(directorsService.getDirectorsAboveThreshold(0L))
                .then(() -> thirdPagePublisher.emit(thirdPage))
                .then(() -> secondPagePublisher.error(new RuntimeException("Page 2 failure")))
                .expectNext(DirectorsResponse.of(List.of("Director Alpha", "Director Omega")))
                .verifyComplete();

        verify(moviesApiClient, times(1)).fetchMoviesPage(1);
        verify(moviesApiClient, times(1)).fetchMoviesPage(2);
        verify(moviesApiClient, times(1)).fetchMoviesPage(3);
    }

    @Test
    void testGetDirectorsAboveThreshold_HandlesLargeDatasetEfficiently() {
        int totalPages = 5;
        int pageSize = 500;

        for (int page = 1; page <= totalPages; page++) {
            List<Movie> pageData = IntStream.range(0, pageSize)
                    .mapToObj(index -> createMovieWithDirector(index % 2 == 0 ? "Director A" : "Director B"))
                    .collect(Collectors.toList());

            MoviesPageResponse response = createMoviesPageResponse(pageData, page, totalPages);
            when(moviesApiClient.fetchMoviesPage(page)).thenReturn(Mono.just(response));
        }

        StepVerifier.create(directorsService.getDirectorsAboveThreshold(1200L))
                .expectNext(DirectorsResponse.of(List.of("Director A", "Director B")))
                .verifyComplete();

        for (int page = 1; page <= totalPages; page++) {
            verify(moviesApiClient, times(1)).fetchMoviesPage(page);
        }
    }

    private MoviesPageResponse createMoviesPageResponse(
            List<Movie> data,
            int page,
            int totalPages) {
        return MoviesPageResponse.builder()
                .data(data)
                .page(page)
                .perPage(Math.max(1, data != null ? data.size() : 0))
                .total(data != null ? data.size() * totalPages : 0)
                .totalPages(totalPages)
                .build();
    }

    private Movie createMovieWithDirector(String director) {
        return Movie.builder()
                .title("Sample Movie")
                .year(2015)
                .rated("PG-13")
                .released("2015-01-01")
                .runtime("120 min")
                .genre("Drama")
                .director(director)
                .writer("Sample Writer")
                .actors("Sample Actor")
                .build();
    }
}


package integration;

import com.example.movie_directors_service.MovieDirectorsServiceApplication;
import com.example.movie_directors_service.client.MoviesApiClient;
import com.example.movie_directors_service.controller.DirectorsController;
import com.example.movie_directors_service.dto.response.MoviesPageResponse;
import com.example.movie_directors_service.model.Movie;
import com.example.movie_directors_service.service.DirectorsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = {MovieDirectorsServiceApplication.class, MovieDirectorsServiceApplicationTests.StubbedMoviesApiClientConfiguration.class},
        properties = "spring.main.web-application-type=reactive")
@AutoConfigureWebTestClient
class MovieDirectorsServiceApplicationTests {

	@Autowired
	private DirectorsController directorsController;

	@Autowired
	private DirectorsService directorsService;

	@Autowired
	private MoviesApiClient moviesApiClient;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void contextLoads() {
	}

	@Test
	void healthCheck_WhenApplicationStarts_ThenBeansAreAvailable() {
		assertThat(directorsController).isNotNull();
		assertThat(directorsService).isNotNull();
		assertThat(moviesApiClient).isNotNull();
	}

	@Test
	void directorsEndpoint_WhenCalled_ReturnsOkStatus() {
		webTestClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/api/directors")
						.queryParam("threshold", 1)
						.build())
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.directors").isArray()
				.jsonPath("$.directors[0]").isEqualTo("Christopher Nolan");
	}

	@TestConfiguration
	static class StubbedMoviesApiClientConfiguration {

		@Bean
		@Primary
		MoviesApiClient moviesApiClient() {
			return new MoviesApiClient(WebClient.builder(), "http://localhost", 5L, 0, 0L) {
				@Override
				public Mono<MoviesPageResponse> fetchMoviesPage(int page) {
					MoviesPageResponse singlePage = MoviesPageResponse.builder()
							.page(1)
							.perPage(2)
							.total(2)
							.totalPages(1)
							.data(List.of(
									Movie.builder()
										.title("Inception")
										.year(2010)
										.rated("PG-13")
										.released("16 Jul 2010")
										.runtime("148 min")
										.genre("Sci-Fi")
										.director("Christopher Nolan")
										.writer("Christopher Nolan")
										.actors("Leonardo DiCaprio")
										.build(),
									Movie.builder()
										.title("Interstellar")
										.year(2014)
										.rated("PG-13")
										.released("07 Nov 2014")
										.runtime("169 min")
										.genre("Sci-Fi")
										.director("Christopher Nolan")
										.writer("Jonathan Nolan")
										.actors("Matthew McConaughey")
										.build()))
							.build();
					return Mono.just(singlePage);
				}

			};
		}
	}
}


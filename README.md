# Movie Directors Service

Spring Boot microservice that aggregates a paginated movies catalog and surfaces directors whose filmography exceeds a configurable threshold.

---

## Feature Highlights

- Aggregates movies across all pages of `https://wiremock.dev.eroninternational.com/api/movies/search`
- Counts movies per director and returns those strictly above the requested threshold
- Orders directors alphabetically for deterministic results
- Fetches pages concurrently with configurable parallelism for low latency
- Provides structured error responses and graceful degradation

## Bonus Features Implemented

- **Resilience:** Configurable timeouts, retries with exponential backoff, and circuit-breaker friendly design
- **Health Check:** Fail-fast availability probe that verifies the external Movies API before serving requests
- **Error Handling:** Custom `ExternalApiException`, descriptive `ErrorResponse`, and mapping of downstream errors to `503` or `500`
- **Performance:** Non-blocking WebClient with Reactor, batching of requests, and capped concurrency to protect the upstream API
- **Code Quality:** Layered architecture, DTO separation, and integration-ready configuration via `application.properties`
- **Testing:** Unit tests for aggregation logic and service-level edge cases (run with `mvn test`)

## Architecture Overview

```
Client → REST Controller → Directors Service → Movies API Client → External Movies API
```

- **Controller layer (`DirectorsController`)** exposes `GET /api/directors`
- **Service layer (`DirectorsService`)** orchestrates pagination, aggregation, and threshold filtering
- **Client layer (`MoviesApiClient`)** handles reactive HTTP calls and pagination metadata

## Technology Stack

- Spring Boot 3.5.7 + Spring WebFlux (WebClient)
- Project Reactor for reactive, non-blocking flows
- Jackson for JSON serialization
- Maven for build/test lifecycle
- Java 17+ runtime (21 recommended)

## Project Structure

```
src/main/java/com/example/eron_directors_service/
├── controller/             # REST endpoints
├── service/                # Business logic and aggregation
├── client/                 # External API integration
├── dto/                    # Request/response contracts
├── exception/              # Custom exceptions and error models
└── model/                  # Domain entities
```

## Configuration

Configure external dependencies in `src/main/resources/application.properties`:

```properties
movies.api.base-url=https://wiremock.dev.eroninternational.com
movies.api.timeout=30s          # Total request timeout
movies.api.retry.max-attempts=3 # Initial call + 2 retries
movies.api.max-concurrency=8    # Aligns with PAGE_FETCH_CONCURRENCY in the service layer
```

Environment overrides can be supplied via JVM system properties or environment variables (e.g., `MOVIES_API_BASE_URL`).

## Getting Started

### Prerequisites
- Java 17 or higher (Java 21 recommended)
- Maven 3.6+

### Run with Maven Wrapper
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

### Run with Maven (installed globally)
```bash
mvn spring-boot:run
```

### Build & Run JAR
```bash
mvn clean package
java -jar target/eron-directors-service-0.0.1-SNAPSHOT.jar
```

The service listens on `http://localhost:8080` by default.

## API Reference

### GET `/api/directors`

Returns directors whose movie count is strictly greater than the provided threshold.

| Parameter   | Type    | Required | Validation                                   | Description |
|-------------|---------|----------|-----------------------------------------------|-------------|
| `threshold` | integer | ✅       | `@Min(0)` and `@Max(500)`                      | `threshold=0` returns directors with ≥1 movie; higher values filter accordingly |

#### Example Request
```bash
curl "http://localhost:8080/api/directors?threshold=5"
```

#### Example Response
```json
{
  "directors": ["Martin Scorsese", "Woody Allen"]
}
```

#### Response Codes

- `200 OK` – Successful aggregation
- `400 Bad Request` – Missing or invalid `threshold`
- `503 Service Unavailable` – Downstream API unreachable or timed out
- `500 Internal Server Error` – Unexpected server condition

##  Resilience & Edge Cases

- Skips blank or null director names during aggregation
- Applies 30s timeout with retries for transient upstream failures
- Returns empty list when no directors exceed the threshold
- Logs structured errors for observability and troubleshooting

## Testing

Run the test suite:
```bash
mvn test
```

## Future Enhancements

- **Performance:** Adaptive concurrency & caching of recent pages
- **Monitoring:** Micrometer metrics, distributed tracing, and health indicators
- **Features:** Filtering by year/genre, pagination on response, GraphQL endpoint
- **Deployment:** Containerization with Docker, Helm charts, canary rollout strategy


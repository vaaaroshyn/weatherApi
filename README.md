# Weather Sensor API

REST API for registering weather sensors and collecting their temperature measurements.

## Features

- Register sensors by unique name.
- Submit measurements for registered sensors.
- Store measurement timestamp on the server.
- List measurements with pagination and sorting.
- Count rainy measurements using a database query.
- Validate request payloads and return structured API errors.
- Manage PostgreSQL schema with Flyway migrations.
- Run the API and PostgreSQL locally with Docker Compose.
- Explore the API through Swagger UI.

## Technology Stack

- Java 17
- Spring Boot 3.1.2
- Spring Web
- Spring Data JPA
- Bean Validation
- PostgreSQL
- Flyway
- Springdoc OpenAPI
- Maven Wrapper
- JUnit 5, Mockito, MockMvc
- Testcontainers with PostgreSQL
- Docker Compose

## Architecture

The application follows a small layered Spring Boot structure:

- `controllers` expose REST endpoints under `/api/v1`.
- `dto` contains request, response, page, and error payloads.
- `services` contain business checks, transactions, and persistence orchestration.
- `repositories` provide Spring Data JPA access to PostgreSQL.
- `models` contain JPA entities mapped to the Flyway-managed schema.
- `exceptions` contains domain exceptions and centralized REST error handling.

POST endpoints return `201 Created` with an empty response body and a `Location` header. GET endpoints return response DTOs, not JPA entities.

## Requirements

- Java 17
- Docker and Docker Compose for the containerized setup
- Maven is optional because the project includes Maven Wrapper scripts
- PostgreSQL is required only when running without Docker

## Quick Start With Docker Compose

Build and start PostgreSQL plus the API:

```bash
docker compose up --build
```

The API is exposed at:

```text
http://localhost:8080
```

Stop the environment without deleting the PostgreSQL volume:

```bash
docker compose down
```

Validate the Compose file:

```bash
docker compose config
```

The Compose setup uses development-only default credentials. Override them with environment variables or a local `.env` file when needed. Do not commit real secrets.

## Running Without Docker

Start a local PostgreSQL database and make sure the database referenced by `DB_URL` exists. Then run the application:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Example PowerShell environment setup:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/weather_db"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"
.\mvnw.cmd spring-boot:run
```

## Environment Variables

| Variable | Description | Default |
| --- | --- | --- |
| `DB_URL` | JDBC URL for PostgreSQL | `jdbc:postgresql://localhost:5432/weather_db` |
| `DB_USERNAME` | PostgreSQL username | `postgres` |
| `DB_PASSWORD` | PostgreSQL password | `postgres` |

The defaults are local development placeholders, not production credentials.

## Database Migrations

Flyway migrations are stored in:

```text
src/main/resources/db/migration
```

The initial migration creates:

- `sensors`
- `measurements`
- `idx_measurements_sensor_id`

Spring Boot runs Flyway automatically on application startup. Hibernate is configured with:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

This validates JPA mappings against the migrated schema instead of creating or updating tables.

## API Endpoints

| Method | Path | Description | Success |
| --- | --- | --- | --- |
| `POST` | `/api/v1/sensors` | Register a sensor | `201 Created` |
| `POST` | `/api/v1/measurements` | Add a measurement for a registered sensor | `201 Created` |
| `GET` | `/api/v1/measurements` | Get measurements with `page`, `size`, and `sort` | `200 OK` |
| `GET` | `/api/v1/measurements/rainy-days/count` | Count rainy measurements | `200 OK` |

Relevant error statuses:

- `400 Bad Request` for Bean Validation errors, malformed JSON, or wrong JSON value types.
- `404 Not Found` when a measurement references an unknown sensor.
- `409 Conflict` when registering a duplicate sensor name.

## JSON Examples

Register a sensor:

```json
{
  "name": "sensor-1"
}
```

Add a measurement:

```json
{
  "value": 23.4,
  "raining": false,
  "sensor": {
    "name": "sensor-1"
  }
}
```

Page response:

```json
{
  "content": [
    {
      "id": 1,
      "value": 23.4,
      "raining": false,
      "measuredAt": "2026-09-03T09:15:30",
      "sensor": {
        "id": 1,
        "name": "sensor-1"
      }
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

API error:

```json
{
  "timestamp": "2026-09-03T09:15:30Z",
  "status": 400,
  "error": "Bad Request",
  "message": "name - The name should not be blank!",
  "path": "/api/v1/sensors"
}
```

## Swagger and OpenAPI

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

## Running Tests

Run the full verification lifecycle:

```bash
./mvnw verify
```

On Windows PowerShell:

```powershell
.\mvnw.cmd verify
```

The main integration tests use Testcontainers with PostgreSQL. They do not use H2 and do not require a manually started PostgreSQL instance. If Docker is unavailable, those tests are skipped by Testcontainers configuration.

## Project Structure

```text
.
|-- compose.yaml
|-- Dockerfile
|-- pom.xml
|-- src
|   |-- main
|   |   |-- java/com/spring/myapp
|   |   |   |-- config
|   |   |   |-- controllers
|   |   |   |-- dto
|   |   |   |-- exceptions
|   |   |   |-- models
|   |   |   |-- repositories
|   |   |   `-- services
|   |   `-- resources/db/migration
|   `-- test
|       |-- java/com/spring/myapp
|       `-- resources
`-- README.md
```

## Future Improvements

The following items are not implemented:

- Authentication and authorization.
- CI pipeline.
- Cloud deployment configuration.
- Rate limiting.
- Observability with metrics and tracing.
- API version migration strategy beyond the current `/api/v1` routes.

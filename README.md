# Task Catalog Service

Production-style REST service for task management.

## Stack
- Kotlin 1.9
- Spring Boot 3
- Spring MVC
- Project Reactor (`Mono` in service layer)
- Spring JDBC `JdbcClient`
- Native SQL (no JPA/ORM)
- PostgreSQL
- Flyway
- Swagger/OpenAPI (`springdoc`)
- JUnit 5, Mockito, Reactor Test

## Features
- Create task: `POST /api/v1/tasks`
- Get task by id: `GET /api/v1/tasks/{id}`
- Get paged list with optional status filter: `GET /api/v1/tasks?page=0&size=10&status=NEW`
- Update only status: `PATCH /api/v1/tasks/{id}/status`
- Delete task: `DELETE /api/v1/tasks/{id}`

## Validation and Errors
- `title` is required
- `title` length: 3..100
- Centralized error handling via `@RestControllerAdvice`
- `404 Not Found` for missing task by id

## Run locally
```bash
./mvnw spring-boot:run
```

## Run with Docker Compose (PostgreSQL + backend)
```bash
docker compose up --build -d
```

Compose reads variables from `.env`:
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `DB_PORT`
- `APP_PORT`
- `DB_DRIVER`
- `DB_URL`

## API docs
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

## Tests
```bash
./mvnw test
```

Note: integration tests use in-memory H2 only in the test scope.

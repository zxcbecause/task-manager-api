# Task Manager API

![Java](https://img.shields.io/badge/Java-21-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue) ![Docker](https://img.shields.io/badge/Docker-ready-2496ED)

A REST API for managing personal tasks, built with Spring Boot 3, Spring Data JPA and PostgreSQL.
It has validation, error responses in the standard format (RFC 7807), pagination, filtering, OpenAPI docs and a Docker setup.

## Tech stack

- Java 21, Spring Boot 3 (Web, Data JPA, Validation, Actuator)
- H2 (default, in-memory) or PostgreSQL (`postgres` profile)
- springdoc-openapi (Swagger UI)
- JUnit 5, Mockito, MockMvc, AssertJ
- Docker, Docker Compose, GitHub Actions

## Features

- CRUD for tasks with `status` (`TODO`, `IN_PROGRESS`, `DONE`), `priority` (`LOW`, `MEDIUM`, `HIGH`) and `dueDate`
- Filtering by status and priority, title search, pagination and sorting
- `overdue` flag calculated on the fly (uses an injected `Clock`, so it is easy to test)
- Stats endpoint with task counts per status
- Validation errors return field-level messages
- Timestamps (`createdAt`, `updatedAt`) managed by JPA lifecycle callbacks

## API

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/tasks?status=&priority=&search=&page=&size=&sort=` | List tasks |
| `GET` | `/api/tasks/{id}` | Get one task |
| `GET` | `/api/tasks/stats` | Counts per status |
| `POST` | `/api/tasks` | Create a task |
| `PUT` | `/api/tasks/{id}` | Update a task |
| `PATCH` | `/api/tasks/{id}/status` | Change status only |
| `DELETE` | `/api/tasks/{id}` | Delete a task |

Interactive docs: http://localhost:8080/swagger-ui.html

### Example

```bash
curl -X POST localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "Prepare for interview", "priority": "HIGH", "dueDate": "2026-10-10"}'
```

```json
{
  "id": 1,
  "title": "Prepare for interview",
  "description": null,
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2026-10-10",
  "overdue": false,
  "createdAt": "2026-09-30T08:15:02.123Z",
  "updatedAt": "2026-09-30T08:15:02.123Z"
}
```

Validation error:

```json
{
  "type": "about:blank",
  "title": "Validation error",
  "status": 400,
  "detail": "Validation failed",
  "instance": "/api/tasks",
  "errors": { "title": "title is required" }
}
```

## Running locally

Requirements: JDK 21+ and Maven, or just Docker.

**With H2 (no setup needed):**

```bash
mvn spring-boot:run
```

**With PostgreSQL in Docker:**

```bash
docker compose up --build
```

Database settings are read from environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`). The defaults in `docker-compose.yml` are only for a local throwaway database.

## Tests

```bash
mvn test
```

- `TaskServiceTest` – unit tests for business logic with Mockito and a fixed clock
- `TaskApiIntegrationTest` – full HTTP tests through MockMvc against an in-memory database

## Project structure

```text
src/main/java/io/github/zxcbecause/taskmanager
├── TaskManagerApplication.java
├── config/            # Clock + OpenAPI beans, global exception handler
└── task/
    ├── Task.java              # JPA entity
    ├── TaskRepository.java    # Spring Data repository with search query
    ├── TaskService.java       # business logic
    ├── TaskController.java    # REST endpoints
    └── dto/                   # request/response records
```

## Possible improvements

- Authentication with Spring Security + JWT, tasks per user
- Flyway migrations instead of `ddl-auto`
- Testcontainers for PostgreSQL integration tests

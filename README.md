# Shift Planner

A Spring Boot application for managing teams, memberships, and employee schedules, designed for extensibility and future frontend integration.

## Current Capabilities

- JWT-based authentication
- User management
- Team management with role-based team permissions
- Shift creation and updates with validation
- Interactive API documentation via Swagger UI

## Tech Stack

- Java 25
- Spring Boot 4
- Spring Security
- Spring Data JPA
- PostgreSQL + Flyway
- springdoc OpenAPI / Swagger UI
- JUnit, Mockito, Testcontainers
- Docker Compose

## Getting Started

1. Create a root `.env` file with database and JWT settings (you can start from [.env.example](./.env.example)).
2. Run `docker compose up --build`.
3. Open Swagger UI and log in with the [seeded admin](./backend/README.md#default-local-admin-account) account.

For more details about the backend setup, see [backend/README.md](./backend/README.md).

Useful URLs:

- API base URL: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

# Shift Planner

Shift Planner is a scheduling and team management project built around a shared Spring Boot backend and frontend clients. This repository is intended to hold the core API plus multiple frontend implementations over time.

## Repository Layout

- `backend/` - Spring Boot REST API, database migrations, security, and business logic
- `web-angular/` - Angular frontend example

Additional frontend clients can be added later as separate modules alongside `web-angular/`.

## Current Capabilities

- JWT-based authentication
- User management
- Team management and memberships
- Shift management in the backend API
- Swagger UI for API exploration

## Tech Stack

- Backend: Java 25, Spring Boot 4, Spring Security, Spring Data JPA, PostgreSQL, Flyway
- Frontend example: Angular 21, standalone components, Angular Router, Reactive Forms
- Tooling: Docker Compose, Vitest, JUnit, Mockito, Testcontainers

## Getting Started

1. Create a root `.env` file from [.env.example](./.env.example).
2. Start the local stack from the repository root:

```bash
docker compose up --build
```

3. Use the backend directly through Swagger UI or run a frontend client separately.

Useful local URLs:

- Backend API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Angular frontend: `http://localhost:4200`

## Module Readmes

- Backend setup and API details: [backend/README.md](./backend/README.md)
- Angular frontend setup: [web-angular/README.md](./web-angular/README.md)

## Default Local Admin Account

- Email: `admin@shiftplanner.local`
- Password: `admin123`

Use these credentials in Swagger or in any local frontend client connected to the backend.

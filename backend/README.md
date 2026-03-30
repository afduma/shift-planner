# Shift Planner Backend

This module contains the Spring Boot API for the Shift Planner application. It handles authentication, user and team management, membership-based authorization, and shift scheduling backed by PostgreSQL.

## API Endpoints

Main resource groups:

- `POST /api/auth/login`
- `GET|POST|PUT|DELETE /api/users`
- `GET|POST|PUT /api/teams`
- `GET|POST|PUT|DELETE /api/teams/{teamId}/memberships`
- `GET /api/users/{userId}/memberships`
- `GET|POST|PUT|DELETE /api/shifts`

Swagger UI is available at:

`http://localhost:8080/swagger-ui/index.html`

## Environment Variables

For local development and Docker Compose, environment variables are provided via the repository root `.env` file.

Required keys:

```env
DB_NAME=shiftplanner
DB_USER=shiftplanner
DB_PASSWORD=shiftplanner
DB_PORT=5433
JWT_SECRET=replace-with-a-long-random-secret
```

Notes:

- `JWT_SECRET` should be a long random value for local development.
- The datasource defaults to `jdbc:postgresql://localhost:${DB_PORT}/${DB_NAME}`.
- If you keep the defaults above, the included Docker Compose file works without further changes.

## Installation

### Demo Startup

From the repository root:

```bash
docker compose up --build
```

This starts PostgreSQL and the backend application together.

### Local Development Startup

1. Create the root `.env` file with the required variables.
2. Start PostgreSQL from the repository root:

```bash
docker compose up -d postgres
```

3. Start the backend:

```bash
cd backend
./mvnw spring-boot:run
```

Flyway migrations run automatically on startup.

Run tests with:

```bash
cd backend
./mvnw test
```

Apply formatting with:

```bash
cd backend
./mvnw spotless:apply
```

## Default Local Admin Account

For local/demo use, the application seeds a default admin account on startup if it does not already exist:

- Email: `admin@shiftplanner.local`
- Password: `admin123`

Use these credentials to log in through `POST /api/auth/login`, then send the returned bearer token in authenticated requests.

## Design Notes

- Domain responsibilities are separated by feature package.
- Access rules are enforced centrally through security configuration.
- Database schema changes are versioned with Flyway migrations.
- DTOs and mapper classes keep the API contract separate from persistence models.


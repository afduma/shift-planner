# Shift Planner Frontend

This module contains the Angular frontend for Shift Planner. It connects to the Spring Boot backend and provides the foundation for authentication, dashboard, users, and teams screens.

## Stack

- Angular 21
- Angular Router
- HttpClient + interceptor-based auth
- Reactive Forms
- Vitest

## Local Development

1. Start the backend from the repository root or from `backend/`.
2. Install frontend dependencies:

```bash
cd web-angular
npm install
```

3. Start the Angular dev server:

```bash
cd web-angular
npm start
```

Frontend URL:

`http://localhost:4200`

By default, the frontend calls:

`http://localhost:8080/api`

That value is configured through Angular environment files and exposed to services through the `API_BASE_URL` injection token.

## Backend Notes

- Backend base URL: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Default local admin:
  - Email: `admin@shiftplanner.local`
  - Password: `admin123`

The backend currently allows CORS from `http://localhost:4200` by default.

## Available Commands

```bash
npm start
npm run build
npm test -- --watch=false
npm run format
npm run format:check
```

## Structure

The app is organized by responsibility:

- `src/app/core` for auth, API services, shared models, and app-level configuration
- `src/app/layout` for the authenticated shell and navbar
- `src/app/features` for route-level feature screens
- `src/environments` for dev/prod frontend configuration

## Production Build

Create a production build with:

```bash
cd web-angular
npm run build
```

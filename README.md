# Bloom

Personal fitness dashboard — aggregates Garmin activities, body measurements, and a custom workout library.

## Stack

- **Backend:** Java 25, Spring Boot 4.0.6, Maven
- **Frontend:** Angular 17+, standalone components
- **Database:** PostgreSQL 18
- **Local dev:** Docker Compose

## Getting started

### Prerequisites

- Docker Desktop
- Java 25
- Node.js (LTS) + Angular CLI

### Run locally

1. Start the database:
   ```bash
   docker compose up -d
   ```

2. Start the backend:
   ```bash
   cd bloom-backend
   ./mvnw spring-boot:run
   ```

3. Start the frontend:
   ```bash
   cd bloom-frontend
   ng serve
   ```

- Frontend: http://localhost:4200
- Backend API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui.html

## Environment

Copy `.env.example` to `.env` before running Docker Compose:

```bash
cp .env.example .env
```

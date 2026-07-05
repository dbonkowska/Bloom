# Bloom

Personal fitness dashboard — aggregates Garmin activities, body measurements, and a custom workout library.

## GitHub

Repo: https://github.com/dbonkowska/Bloom

At the start of every session, check open issues to understand what to work on next:

```
gh issue list --repo dbonkowska/Bloom --state open
```

Read the relevant issue before starting work:

```
gh issue view <number> --repo dbonkowska/Bloom
```

## Stack

- Backend: Java 25, Spring Boot 4.0.6, Maven — in `bloom-backend/`
- Frontend: Angular 17+, standalone components — in `bloom-frontend/`
- Database: PostgreSQL via Docker Compose
- No auth (single user, local MVP)

## Dev conventions

- No Lombok — use Java records for DTOs, IDE generation for entities
- No comments unless the WHY is non-obvious
- TDD: write tests first
- Every domain has a service layer — controllers are HTTP-only adapters, services own business logic and DTO mapping
- Integration tests use `@SpringBootTest` + Testcontainers + MockMvc (one per domain); `@DataJpaTest` only for Specification predicate tests

## PR naming

Use conventional commits format: `<type>: <short description>`

Types: `feat`, `fix`, `chore`, `refactor`, `test`, `docs`

Examples:
- `feat: Activity domain — entity, repository, GET /api/activities`
- `fix: correct date-range boundary inclusion in activity query`
- `chore: scaffold backend, frontend and database`

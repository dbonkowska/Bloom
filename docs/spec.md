# Bloom — Spec

## Problem

Fitness data lives across multiple apps (Garmin Connect, MovinLife scale app, Fitiful). No single app shows everything together. Workout libraries are locked inside subscriptions. No way to compose custom plans from multiple sources.

## Goals

- One place to see all fitness data: activities, body measurements, training plans
- Import data from exports (no API access needed)
- Build and manage workout library with YouTube references (no downloads)
- Compose training plans from library workouts
- Calendar view combining Garmin activities and planned sessions
- Body measurements over time

## Out of scope (for now)

- LLM features (separate integration later)
- Mobile app
- Social features

---

## Data Model

### Activity
Imported from Garmin export files.
- date, type (run/bike/swim/etc.), duration, distance, avg HR, calories, source file name

### BodyMeasurement
Imported from scale app (MovinLife) export files.
- date, weight, body fat %, muscle mass, water %, BMI

### Workout
Single exercise session template. Can be a full workout or a single exercise.
- name, YouTube URL, duration (minutes), muscle groups (tags), notes, source (e.g. "Fitiful", "own")

### TrainingPlan
Ordered collection of Workouts with a schedule pattern.
- name, description, weeks, days per week, plan entries (day → Workout)

### PlannedSession
A Workout assigned to a specific date on the calendar.
- date, workout (ref), training plan (ref, optional), completed (bool), notes

---

## Features

### Import
- Upload Garmin CSV (activity list export) → parse → save as Activities
- Duplicate detection by date + type
- FIT format: future option for route maps / detailed HR zones

### Body Measurements
- Manual entry only (MovinLife exports PDF only)
- Tracking starts from 2025-05-01, no historical data needed

### Workout Library
- CRUD for Workouts
- Filter by muscle group, duration, source
- YouTube URL stored as reference only (no download)

### Training Plans
- Create plan, add workouts per day
- Clone an existing plan to modify
- Assign plan to a start date → generates PlannedSessions

### Calendar
- Month/week view
- Shows Garmin Activities (actual) and PlannedSessions (planned)
- Click to see details
- Mark PlannedSession as completed

### Body Measurements
- Chart: weight over time, body fat % over time
- Manual entry only

---

## Tech Stack

| Layer | Tech |
|-------|------|
| Backend | Java 25, Spring Boot 4.0.6 |
| Frontend | Angular 17+ |
| Database | PostgreSQL |
| Local dev | Docker Compose |
| Auth | None for MVP (single user, local) |

---

## First Tickets

1. `[setup]` Spring Boot + Angular scaffold, PostgreSQL + Docker Compose for local DB
2. `[backend]` Activity domain: model, repository, REST endpoints (list, filter by date range)
3. `[backend]` Garmin CSV import: file upload → parse → save as Activities
4. `[backend]` BodyMeasurement CRUD (manual entry only)
5. `[backend]` Workout library CRUD
6. `[backend]` Calendar API: `GET /calendar?from=&to=` returns Activities + PlannedSessions merged by date
7. `[frontend]` Angular project setup, routing skeleton, basic layout
8. `[frontend]` Calendar view component
9. `[frontend]` Workout library page
10. `[frontend]` Body measurements chart

---

## Decisions

- Angular: standalone components (Angular 17+, no NgModules)

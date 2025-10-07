## Uphill - Technical Documentation

This document provides a comprehensive technical overview of the Uphill system, including architecture, core modules, class and sequence diagrams, flow charts, lifecycle states, API surface, persistence, security, idempotency, configuration, deployment, CI/CD, and known limitations.

## Quick Start

**Prerequisites:** Java 21, Docker & Docker Compose

### Development (Local)
```bash
make dev
./mvnw spring-boot:run -Dspring.profiles.active=local
```
API: http://localhost:8080

### Docker (Full)
```bash
make run
```
API: http://localhost:8080

**Commands:**
- `make logs` - view logs
- `make down` - stop services  
- `make unit-test` - run tests

### System Overview

- Spring Boot application with layered architecture and scheduled background processing
- Core domains: Appointment booking, Outbox event processing, Activity logging
- Entrypoints: REST controllers under `com.uphill.entrypoint.rest`
- Persistence: PostgreSQL (JPA/Hibernate) with Flyway migrations; Redis used for idempotency/cache
- Asynchronous side effects: Outbox pattern with scheduled processor
- Security: JWT-based authentication and method security; selective public endpoints

### High-Level Architecture

![Architecture](docs/diagrams/architecture.svg)

### Core Modules and Responsibilities

- Appointment Management
  - `AppointmentService`: booking flow, resource allocation, activity logging, enqueue outbox events
  - `AppointmentPersistenceService`: save/update appointment state
- Outbox Pattern
  - `OutboxEventService`: create outbox events on domain actions
  - `OutboxProcessingService`: scheduled poller to deliver events to external services and finalize state
- Activity Logging
  - `ActivityLogPersistenceService`: append domain activities
- REST Entrypoints
  - `AppointmentController`: create/list appointments
  - `AuthController`: login/logout
  - `GlobalExceptionHandler`: consistent error responses
- Persistence (Infrastructure)
  - JPA Entities, Spring Data repositories, MapStruct mappers
- Security & Filters
  - `SecurityConfig`, `JwtAuthenticationFilter` (JWT), `IdempotencyFilter` (Redis-backed POST idempotency)

### Key Class Diagram

![Class Diagram](docs/diagrams/class.svg)

### Sequence Diagram: Create Appointment

![Create Appointment Sequence](docs/diagrams/seq-create-appointment.svg)

### Sequence Diagram: Outbox Processing

![Outbox Sequence](docs/diagrams/seq-outbox.svg)

### Flowchart: Appointment Booking

![Booking Flow](docs/diagrams/flow-booking.svg)

### Lifecycle: Appointment Status

![Appointment State](docs/diagrams/state-appointment.svg)

### REST API Surface (selected)

- POST `/api/appointments` (public)
  - Request: patientId, doctor.specialtyId, date (ISO), timeSlot "HH:mm-HH:mm"
  - Response: `CreateAppointmentResponse`
- GET `/api/appointments` (ADMIN)
  - Filters: `patientId`, `doctorId`, `roomId`, `status`, `startDate`, `endDate`, pageable
- POST `/api/auth/login`
  - Returns JWT token with role ADMIN

Responses are consistently wrapped with `ApiResponse<T>`.

### Persistence and Data Model

- JPA Entities: Appointment, Patient, Doctor, Room, TimeSlot, OutboxEvent, ActivityLog
- Repositories: Spring Data JPA with custom queries (filters, status updates)
- Mapping: `EntityMapper` (MapStruct) converts JPA entities to domain models
- Migrations: Flyway scripts in `src/main/resources/db/migration`

### Outbox Pattern Details

- `OutboxEventEntity` stores JSONB payload, status, retryCount, nextRetryAt
- `OutboxEventService` creates events on appointment creation
- `OutboxProcessingService` scheduled poller:
  - Parses payload, dispatches by `eventType`
  - On success: marks event PROCESSED, logs activity, may transition appointment to CONFIRMED
  - On failure: increments retry, sets `nextRetryAt`, logs failure (implementation-dependent)

### Security

- `SecurityConfig` permits `/actuator/**`, `/api/auth/**`, `POST /api/appointments`; all others require auth
- `JwtAuthenticationFilter` validates Bearer token, loads admin via `AdminService`, sets `ROLE_ADMIN` in context
- Stateless sessions; method-level security via `@PreAuthorize`

### Idempotency for Appointment Creation

- `IdempotencyFilter` applies to `POST /api/appointments`
  - Uses `Idempotency-Key` header
  - Reserves key in Redis with short TTL during processing
  - If key is processing: returns 409
  - If cached response exists: returns cached 200/201 body
  - On success: caches body for 24h; on failure/exceptions: clears reservation

### Configuration

- Profiles: default, `local`, `docker`
- Properties: PostgreSQL datasource, Hibernate settings, Redis, JWT, Actuator exposure
- External mock service base URL: `external.mock.base-url`

### Deployment (Docker & Compose)

- Multi-stage Dockerfile builds the JAR with Maven and runs with profile `docker`
- `docker-compose.yml` services:
  - Postgres 16 with healthcheck
  - Redis 7 with LRU policy
  - App container exposing 8080 with healthcheck
  - Mock external service (Node.js) on 3001

### CI/CD (Jenkins)

- Declarative pipeline (`Jenkinsfile`)
  - Parameters for `AWS_REGION`, `IMAGE_TAG`, `DESIRED_COUNT`
  - Stages: Checkout → Prepare env (hydrate `deploy/.env`) → Build & Deploy (`deploy/deploy.sh`)
  - Archives `deploy/**` artifacts post-build

### Known Limitations and Assumptions

- Fixed time slots: Appointments must match predefined `TimeSlot` records; no arbitrary durations
- Timezone handling: Not explicitly addressed; time comparisons assume consistent server TZ
- Idempotency scope: Only `POST /api/appointments` covered by filter; other mutating endpoints not idempotent
- Outbox delivery guarantees: At-least-once; downstream services must be idempotent
- Retry/backoff: Basic fields present; no exponential backoff/DLQ included
- Security surface: `POST /api/appointments` is public; rate limiting and abuse protection not implemented
- Availability constraints: Doctor/Room selection based on time slot/date; no double-booking across fine-grained overlaps beyond slots
- Email/notifications: Success assumed on service response; no bounce or tracking

### Local Development

- Run Postgres and Redis via Docker Compose or locally
- Profiles:
  - `local`: `spring.jpa.hibernate.ddl-auto=update`, Flyway enabled
  - `docker`: externalized datasource/redis via environment variables
- Entry point: `UphillApplication` with `@EnableScheduling`

### Diagrams: Render Locally

- Prerequisite: Docker installed
- Render all diagrams to SVGs:
  - Run: `make docs-diagrams`
- Outputs written to `docs/diagrams/*.svg`; README embeds these images

### References (Code)

- `com.uphill.UphillApplication` – application bootstrap and scheduling
- `com.uphill.core.application.service.appointment.AppointmentService` – booking flow and outbox emission
- `com.uphill.core.application.service.appointment.OutboxProcessingService` – outbox worker
- `com.uphill.entrypoint.rest.appointments.AppointmentController` – REST endpoints
- `com.uphill.infrastructure.persistence.*` – entities, repositories, mappers
- `com.uphill.entrypoint.rest.auth.security.JwtAuthenticationFilter` – JWT auth
- `com.uphill.infrastructure.filter.IdempotencyFilter` – idempotency for create appointment
- `docker-compose.yml`, `Dockerfile`, `deploy/deploy.sh`, `Jenkinsfile` – runtime and CI/CD



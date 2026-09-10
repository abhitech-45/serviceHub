# Implementation Plan: ServiceHub AI Service Portal

**Branch**: `001-service-portal-ai` | **Date**: 2026-09-07 | **Spec**: [spec.md](spec.md)

## Summary

Build ServiceHub AI as a modular monolith with a Spring Boot 3.x Java 21 backend, PostgreSQL persistence, and a React/TypeScript/Vite frontend. Deliver the secure customer request vertical slice first, then extend the same versioned contracts and domain services for agents, administrators, notifications, reporting, and AI self-service.

## Technical Context

**Language/Version**: Java 21; TypeScript with latest stable React available at implementation time

**Primary Dependencies**: Spring Boot 3.x, Spring Security, JWT library, Spring Data JPA, Hibernate, Jakarta Validation, Springdoc OpenAPI, React, Vite, React Router, Axios, approved component system

**Storage**: PostgreSQL with versioned migrations

**Testing**: JUnit 5, Spring Boot Test, repository/integration tests, API contract tests, frontend unit/component tests, Playwright end-to-end tests

**Target Platform**: Local development first; cloud-ready web application

**Project Type**: Web application with separate backend API and frontend client

**Performance Goals**: 95% of normal request views usable within 2 seconds; committed changes visible within 5 seconds

**Constraints**: Versioned `/api/v1` REST API, least-privilege RBAC, ownership checks, global safe errors, audit logging, no secrets in logs, responsive UI

**Scale/Scope**: Three roles, seven user stories, ten core entities, incremental modular-monolith delivery

## Constitution Check

- **Layered boundaries**: PASS. Backend modules separate API, application, domain, persistence, and adapters; frontend separates routes, features, and API access.
- **Security and least privilege**: PASS. JWT, password hashing, RBAC, ownership checks, audit records, and confirmed AI actions are foundational tasks.
- **Testable delivery**: PASS. Every story has independent acceptance tests; contract, integration, security, and UI tests are planned.
- **Observable/versioned operations**: PASS. `/api/v1`, structured errors, correlation IDs, audit events, notification state, and OpenAPI are planned.
- **Simplicity/accessibility**: PASS. Modular monolith, provider ports, responsive states, and local deterministic adapters avoid premature infrastructure.

## Project Structure

```text
backend/
├── pom.xml
├── src/main/java/com/servicehubai/
│   ├── common/             # errors, validation, correlation, shared response types
│   ├── config/             # application and OpenAPI configuration
│   ├── security/           # JWT, password hashing, RBAC, ownership context
│   ├── audit/              # audit domain and persistence
│   ├── user/               # users, roles, authentication
│   ├── request/            # service requests, comments, lifecycle
│   ├── notification/       # notification ports and delivery state
│   └── chat/               # provider-neutral AI boundary and chat persistence
│   └── src/main/resources/db/migration/
└── src/test/

frontend/
├── package.json
├── src/
│   ├── app/                # router, providers, shell
│   ├── api/                # Axios client and typed contracts
│   ├── auth/               # session and protected routes
│   ├── components/         # shared UI
│   ├── features/requests/  # customer request workflows
│   ├── features/dashboard/
│   └── features/chat/
└── tests/

specs/001-service-portal-ai/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
└── tasks.md
```

## Delivery Phases

1. Setup and local PostgreSQL configuration.
2. Foundational API, validation, errors, OpenAPI, migrations, security, RBAC, audit, and frontend shell.
3. US1 authentication and role-aware access.
4. US2 customer request lifecycle and history.
5. US3 agent workflow and notifications.
6. US4 administration and US6 reporting/dashboard metrics.
7. US5 AI self-service and US7 notification completion.
8. Cross-cutting hardening, responsive/accessibility validation, performance checks, and cloud-readiness documentation.

## Complexity Tracking

No constitution violations are proposed. A modular monolith is selected over multiple services because the initial scope is greenfield, local-first, and requires shared authorization, audit, and request lifecycle transactions.

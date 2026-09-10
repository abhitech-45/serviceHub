# Tasks: ServiceHub AI Service Portal

**Input**: Design documents from `/specs/001-service-portal-ai/`

## Phase 1: Setup

- [x] T001 Create backend Maven project in `backend/pom.xml` for Java 21 and Spring Boot 3.x.
- [x] T002 [P] Create frontend Vite TypeScript project in `frontend/package.json` with `build` and `test` scripts.
- [x] T003 [P] Add local PostgreSQL orchestration and safe environment examples in `docker-compose.yml`, `.env.example`, and `README.md`.
- [ ] T004 [P] Add initial backend/frontend ignore and formatting configuration in `.gitignore`, `backend/.editorconfig`, and `frontend/.editorconfig`.

## Phase 2: Foundational

- [ ] T005 Configure Flyway, PostgreSQL profiles, and base application settings in `backend/src/main/resources/application.yml` and `backend/src/main/resources/db/migration/`.
- [x] T006 [P] Implement versioned API routing, problem responses, validation, and global exception handling in `backend/src/main/java/com/servicehubai/common/`.
- [x] T007 [P] Implement JWT security configuration, password hashing, authentication context, and RBAC in `backend/src/main/java/com/servicehubai/security/`.
- [x] T008 [P] Implement OpenAPI metadata, correlation IDs, and structured logging configuration in `backend/src/main/java/com/servicehubai/config/`.
- [ ] T009 Create users, roles, sessions, and audit persistence models and migrations in `backend/src/main/java/com/servicehubai/user/`, `backend/src/main/java/com/servicehubai/audit/`, and `backend/src/main/resources/db/migration/`.
- [x] T010 [P] Create frontend API client, persisted session handling, protected workspace state, and responsive shell in `frontend/src/api/client.ts`, `frontend/src/App.tsx`, and `frontend/src/styles.css`.

## Phase 3: User Story 1 - Register and Access the Portal (P1)

- [x] T011 [P] [US1] Add authentication contract tests in `backend/src/test/java/com/servicehubai/user/AuthControllerContractTest.java`.
- [ ] T012 [P] [US1] Add authentication service tests for registration, duplicate email, login, logout, and password non-disclosure in `backend/src/test/java/com/servicehubai/user/AuthServiceTest.java`.
- [x] T013 [US1] Implement user registration, login, logout, and current-user endpoints in `backend/src/main/java/com/servicehubai/user/api/AuthController.java`.
- [x] T014 [US1] Implement authentication application services and repositories in `backend/src/main/java/com/servicehubai/user/application/` and `backend/src/main/java/com/servicehubai/user/infrastructure/`.
- [ ] T015 [US1] Implement frontend registration, login, logout, and protected routes in `frontend/src/auth/`.
- [ ] T016 [US1] Add frontend authentication tests in `frontend/tests/auth.test.tsx`.

## Phase 4: User Story 2 - Submit and Track a Service Request (P1)

- [ ] T017 [P] [US2] Add request lifecycle and ownership integration tests in `backend/src/test/java/com/servicehubai/request/RequestWorkflowTest.java`.
- [x] T018 [P] [US2] Add request API contract tests in `backend/src/test/java/com/servicehubai/request/RequestControllerContractTest.java`.
- [x] T019 [US2] Implement request, category, priority, comment, and history domain models in `backend/src/main/java/com/servicehubai/request/domain/`.
- [x] T020 [US2] Add request schema evolution, repositories, and historical lifecycle records in `backend/src/main/java/com/servicehubai/request/infrastructure/`.
- [x] T021 [US2] Implement request application services with ownership, validation, lifecycle history, audit, and live update rules in `backend/src/main/java/com/servicehubai/request/application/`.
- [x] T022 [US2] Implement versioned request controllers and DTOs in `backend/src/main/java/com/servicehubai/request/api/`.
- [x] T023 [US2] Implement customer dashboard, request list/detail/create/edit, comments, history, and error states in `frontend/src/App.tsx` and `frontend/src/api/client.ts`.
- [ ] T024 [US2] Add request workflow component and end-to-end tests in `frontend/tests/requests.spec.ts`.

## Phase 5: User Story 3 and User Story 7 - Agent Operations and Notifications (P1/P3)

- [x] T025 [US3] Implement assignment, agent queue, status updates, resolution notes, and closure rules in `backend/src/main/java/com/servicehubai/request/`.
- [ ] T026 [US3] Implement agent workbench and dashboard in `frontend/src/features/agent/`.
- [ ] T027 [US7] Implement notification persistence, delivery port, local email sink, and retry state in `backend/src/main/java/com/servicehubai/notification/`.
- [ ] T028 [US3] [US7] Add agent and notification integration tests in `backend/src/test/java/com/servicehubai/notification/` and `backend/src/test/java/com/servicehubai/request/`.

## Phase 6: User Story 4 and User Story 6 - Administration and Reporting (P2)

- [x] T029 [US4] Implement administrator seed access, role-protected overview, and administrator dashboard in `backend/src/main/java/com/servicehubai/admin/`, `backend/src/main/java/com/servicehubai/config/`, and `frontend/src/App.tsx`.
- [x] T030 [US4] Implement administrator request oversight, owner visibility, priority highlighting, status/priority actions, and audited updates in `backend/src/main/java/com/servicehubai/admin/` and `frontend/src/App.tsx`.
- [x] T031 [US6] Implement administrator request metrics and all-request operational overview in `backend/src/main/java/com/servicehubai/admin/`.
- [x] T032 [US4] [US6] Implement administrator overview screen and reporting widgets in `frontend/src/App.tsx` and `frontend/src/api/client.ts`.

## Phase 7: User Story 5 - AI Self-Service (P2)

- [ ] T033 [US5] Implement chat session/message persistence and provider-neutral Spring AI port in `backend/src/main/java/com/servicehubai/chat/`.
- [ ] T034 [US5] Implement FAQ, troubleshooting, confirmed request creation, authorized status lookup, and escalation in `backend/src/main/java/com/servicehubai/chat/application/`.
- [ ] T035 [US5] Implement deterministic AI stub and OpenAI-compatible adapter boundaries in `backend/src/main/java/com/servicehubai/chat/infrastructure/`.
- [ ] T036 [US5] Implement responsive chat UI, confirmation flow, history, and fallback states in `frontend/src/features/chat/`.
- [ ] T037 [US5] Add AI authorization, confirmation, provider-failure, and persistence tests in `backend/src/test/java/com/servicehubai/chat/`.

## Phase 8: Polish and Release Readiness

- [ ] T038 [P] Run backend unit, integration, contract, and security tests; document commands in `specs/001-service-portal-ai/quickstart.md`.
- [ ] T039 [P] Run frontend typecheck, lint, component, accessibility, and end-to-end tests in `frontend/tests/`.
- [ ] T040 [P] Verify responsive behavior, loading/empty/error states, and performance outcomes against `specs/001-service-portal-ai/spec.md`.
- [ ] T041 Update OpenAPI, data model, deployment notes, and operational documentation in `specs/001-service-portal-ai/` and `docs/`.

## Dependencies and Execution Order

Setup -> Foundational -> US1 -> US2. US3/US7 depend on US2. US4/US6 depend on US2 and US3. US5 depends on authentication and request services. Polish depends on all selected stories.

## MVP

T001-T016 deliver the runnable foundation and User Story 1. T017-T024 complete the customer request MVP.

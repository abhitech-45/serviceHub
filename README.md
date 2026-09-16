# Campus Services Hub

Campus Services Hub is a student-facing university support portal. Students can find campus-service answers, create and track support cases, follow a complete request timeline, and receive help from campus administrators and support specialists.

The project is a local-first modular monolith with a Spring Boot API, React/Vite frontend, PostgreSQL persistence, JWT authentication, real-time request events, and an optional Claude Haiku integration with a deterministic local fallback.

## Objectives

- Give students one place to request help from campus departments.
- Make request ownership, status, history, comments, and admin updates transparent.
- Provide optional conversational support for FAQs, troubleshooting, and owned ticket status.
- Give administrators category-filtered operational visibility and controlled request updates.
- Keep local development simple while preserving cloud-ready service boundaries.

## Current Features

### Student experience

- Register, sign in, sign out, and access a protected student workspace.
- Create requests with controlled campus categories and sub-categories:
  - Academic Support
  - Admission Support
  - Scholarship Support
  - Library Services
  - Hostel Services
  - Gym & Sports Center
  - Transport Services
  - IT Helpdesk
  - Examination Cell
  - Placement Cell
  - Finance & Fees
  - General Administration
- Edit eligible open or pending requests.
- Add request comments.
- View total, open, pending, resolved, and recently updated request counts.
- View a vertical request lifecycle timeline with timestamps, actors, and remarks.
- Receive live request updates through Server-Sent Events.
- Use the optional campus support chatbot for troubleshooting and owned ticket status.

### Administrator experience

- View campus-wide request and account metrics.
- Filter the request queue by campus service category.
- Change status and priority using staged edits.
- Assign an active support specialist from a controlled dropdown.
- Add timeline remarks and resolution notes.
- Persist changes only after clicking **Save update**.
- Receive a success confirmation after a committed update.
- Preserve admin actions in the append-only `admin_request_actions` table.

### Request lifecycle

Requests support these lifecycle stages:

```text
OPEN -> RECEIVED -> UNDER_REVIEW -> ASSIGNED_TO_SUPPORT -> IN_PROGRESS
     -> AWAITING_USER_RESPONSE -> USER_RESPONSE_RECEIVED -> RESOLVED -> CLOSED
```

Every status update creates a `request_status_history` record. Existing history is never overwritten.

## Technology Stack

### Backend

- Java 21
- Spring Boot 3.5.16
- Spring Web MVC and Bean Validation
- Spring Security with JWT authentication
- Spring Data JPA and Hibernate
- PostgreSQL for local persistence
- H2 for automated tests
- Anthropic-compatible Claude Haiku provider with deterministic fallback
- Springdoc OpenAPI 2.8.13
- JJWT 0.12.6
- Maven

### Frontend

- React
- TypeScript
- Vite
- Axios
- React Router dependency available for client routing
- Vitest and jsdom
- Responsive CSS with the Campus Services Hub visual system

## Project Structure

```text
serviceHubAI/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/servicehubai/
│       │   ├── admin/       # Administrator operations and admin action persistence
│       │   ├── audit/       # Audit records
│       │   ├── chat/        # Student chatbot endpoint and fallback logic
│       │   ├── common/      # Errors, validation, health, correlation IDs
│       │   ├── config/      # Profiles, CORS, OpenAPI, local account seeding
│       │   ├── request/     # Requests, comments, lifecycle, status history
│       │   ├── security/    # JWT and RBAC
│       │   └── user/        # Registration, login, roles, users
│       └── test/
├── frontend/
│   ├── package.json
│   └── src/
│       ├── App.tsx         # Student/admin portal views and interactions
│       ├── api/client.ts    # Typed API client and SSE subscription
│       └── styles.css       # Responsive university portal visual system
├── docker-compose.yml       # Local PostgreSQL
├── .env.example             # Safe environment variable reference
└── specs/001-service-portal-ai/
    ├── spec.md
    ├── plan.md
    ├── tasks.md
    └── batch-report.yaml
```

## Local Setup

### Prerequisites

- Java 21
- Maven 3.9+
- Node.js 22+
- Docker Desktop, if using PostgreSQL locally

### Start PostgreSQL

```powershell
cd C:\ABHIJECH\Assignment\serviceHubAI
docker compose up -d postgres
```

### Start the backend

```powershell
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Backend base URL: `http://localhost:8081/api/v1`

### Start the frontend

```powershell
cd frontend
npm install
npm run dev
```

Vite normally uses `http://localhost:5173`. If that port is busy, it selects the next available port, such as `http://localhost:5174`.

## Local Accounts

The local profile seeds the administrator account on backend startup:

| Role | Email | Password |
| --- | --- | --- |
| Administrator | `admin@servicehub.local` | `Admin@12345` |
Override the administrator with `ADMIN_EMAIL`, `ADMIN_PASSWORD`, and `ADMIN_DISPLAY_NAME`.

Support-agent assignment remains role-based. The local profile seeds five directory-only support specialists for assignment; they do not have shared login credentials:

- `academic.support@servicehub.local` — Academic Support Desk
- `admissions.support@servicehub.local` — Admissions Support Desk
- `it.support@servicehub.local` — IT Helpdesk
- `student.services@servicehub.local` — Student Services Desk
- `campus.operations@servicehub.local` — Campus Operations Desk

## API Surface

All application routes are versioned under `/api/v1`.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/auth/register` | Register a student account |
| `POST` | `/auth/login` | Start a JWT session |
| `POST` | `/auth/logout` | Revoke a refresh session |
| `GET` | `/users/me` | Read the authenticated profile |
| `GET` | `/health` | Application health response |
| `POST` | `/requests` | Create a student request |
| `GET` | `/requests` | List the authenticated student's requests |
| `GET` | `/requests/{reference}` | Read an owned request |
| `PATCH` | `/requests/{reference}` | Update an eligible request |
| `POST` | `/requests/{reference}/comments` | Add a request comment |
| `GET` | `/requests/{reference}/events` | Subscribe to request SSE updates |
| `GET` | `/admin/overview` | Read administrator metrics |
| `GET` | `/admin/requests` | Read the administrator request queue |
| `GET` | `/admin/support-agents` | List active support specialists |
| `PATCH` | `/admin/requests/{reference}` | Commit an admin request update |
| `POST` | `/chat/messages` | Send an authenticated chatbot message; accepts an optional `sessionId` and returns the persisted session ID, intent, category, suggested priority, answer, and request matches |

Interactive API documentation is available at `http://localhost:8081/swagger-ui.html`.

## Chatbot Configuration

The local profile supports provider-neutral AI configuration. Chat sessions and messages are persisted in
`chat_sessions` and `chat_messages`, while ticket actions remain deterministic and confirmation-gated.
Without provider credentials, the deterministic campus fallback remains available.

To enable Claude Haiku through an Anthropic-compatible Azure AI endpoint, provide credentials only through
the runtime environment:

```powershell
$env:AI_PROVIDER = "anthropic"
$env:ANTHROPIC_API_KEY = "<rotated-key>"
$env:ANTHROPIC_BASE_URL = "https://your-anthropic-endpoint/anthropic"
$env:ANTHROPIC_MODEL = "claude-haiku-4-5"
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The default daily AI provider limit is 15 calls and can be changed with `AI_DAILY_LIMIT`. Provider
responses time out after 10 seconds and fall back to local support behavior. Provider diagnostics are
available at `GET /api/v1/chat/health`, and `POST /api/v1/chat/test` sends `Hello` to verify connectivity.

The assistant recognizes `FAQ_QUERY`, `CREATE_SERVICE_REQUEST`, `CHECK_TICKET_STATUS`,
`TROUBLESHOOTING`, `GENERAL_ASSISTANCE`, and guarded escalation requests. Ticket creation requires
explicit confirmation, status lookup is restricted to the authenticated student's owned requests,
prompt-injection and secret-disclosure requests are refused, AI usage metrics are exposed in the
administrator overview, and chat history is retained for 30 days.

Never commit API keys or paste them into README files, source files, or committed `.env` files. The chatbot requires confirmation before creating a service case and only looks up requests owned by the authenticated student.

## Tests and Build

Backend tests:

```powershell
cd backend
mvn test
```

Frontend tests and production build:

```powershell
cd frontend
npm test
npm run build
```

The test suite covers authentication, request ownership, lifecycle history, admin operations, chat intent/confirmation behavior, OpenAPI startup, and application context loading.

## Configuration Notes

- Local PostgreSQL defaults are defined in `backend/src/main/resources/application-local.yml`.
- Tests use an isolated H2 database defined in `backend/src/test/resources/application-test.yml`.
- The local profile currently uses the repository's existing JPA schema setting; use a migration strategy before production deployment.
- Correlation IDs are returned in responses and included in log context.
- Secrets belong in environment variables, not README files, source files, or committed `.env` files.

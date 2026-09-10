# Research and Decisions

## Decisions

| Area | Decision | Rationale |
|---|---|---|
| Repository shape | Separate `backend/` and `frontend/` | Matches the required Spring API and React client while keeping deployable boundaries clear. |
| Backend build | Maven | Conventional Spring Boot workflow and straightforward CI commands. |
| Database migrations | Flyway | Versioned PostgreSQL changes are repeatable locally and in deployment. |
| UI system | Material UI | Accessible, responsive primitives reduce initial UI infrastructure while allowing a focused visual language. |
| Authentication | Short-lived JWT access token with server-side refresh/session invalidation record | Meets JWT requirement while supporting logout and revocation. |
| Attachments | Local adapter with strict type/size validation in development; provider port for cloud storage/scanning | Avoids unsafe arbitrary file handling and preserves cloud flexibility. |
| Notifications | Persist event and delivery state; local email sink first | Core request changes remain reliable when delivery providers are unavailable. |
| AI | Spring AI-compatible port, deterministic stub for tests, OpenAI-compatible and Azure OpenAI-compatible adapters | Provider neutrality and repeatable tests. |
| Architecture | Modular monolith | Keeps transaction and authorization boundaries simple for the initial release. |

## Authorization Matrix

| Capability | Customer | Agent | Administrator |
|---|---:|---:|---:|
| Register/login/logout | Yes | Admin-provisioned/login | Admin-provisioned/login |
| Create own request | Yes | Optional if assigned policy allows | Yes |
| View own requests | Yes | No unless assigned/authorized | All |
| Update own request | Editable state | Assigned requests | All |
| Add request comment | Own requests | Assigned requests | All |
| Assign requests | No | No | Yes |
| Change status/priority | No | Assigned requests | All |
| Resolve request | No | Assigned requests | All |
| Manage users/roles/taxonomy | No | No | Yes |
| View audit records | No | No | Yes |
| AI status lookup | Own requests only | Assigned requests if enabled | Authorized requests |

## Request Lifecycle

`OPEN -> IN_PROGRESS -> PENDING_CUSTOMER -> IN_PROGRESS -> RESOLVED -> CLOSED`

- Customer edits are allowed while `OPEN` or `PENDING_CUSTOMER`.
- Agent/admin may move an assigned request to `IN_PROGRESS`, `PENDING_CUSTOMER`, or `RESOLVED`.
- `RESOLVED` requires resolution notes and can become `CLOSED` after customer confirmation or administrative closure.
- Invalid transitions are rejected and audited.

## Initial API Conventions

- Base path: `/api/v1`.
- JSON error shape: `type`, `title`, `status`, `detail`, `instance`, `correlationId`, and optional field errors.
- Protected mutations require authenticated identity and role/ownership checks.
- Lists use `page`, `size`, and stable sort fields.
- No endpoint returns passwords, raw tokens, provider secrets, or hidden request data.

## Risks

- Exact AI model and cloud provider remain deployment decisions; adapters isolate the choice.
- Production attachment scanning requires a deployment-specific service; local development uses a bounded adapter.
- SLA calculations require organization-provided target definitions; initial implementation stores configurable targets.

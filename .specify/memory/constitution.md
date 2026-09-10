<!--
Sync Impact Report
- Version change: unratified scaffold -> 1.0.0
- Modified principles: five scaffold placeholders replaced with project principles
- Added sections: Security, Architecture, and Product Constraints; Development Workflow and Quality Gates
- Removed sections: none; scaffold headings were concretized
- Templates requiring updates: plan-template.md (✅ runtime Constitution Check remains compatible); spec-template.md (✅ requirements remain compatible); tasks-template.md (✅ cross-cutting quality tasks remain compatible)
- Command guidance: ✅ existing commands read this constitution at runtime; no command-file changes required
- Follow-up TODOs: none
-->

# ServiceHub AI Constitution

## Core Principles

### I. Layered Architecture and Explicit Boundaries

The backend and frontend MUST maintain clear boundaries between presentation, application
orchestration, domain rules, persistence, and external integrations. Domain behavior MUST NOT
depend directly on transport, persistence, or AI-provider details. Public API contracts,
validation rules, and error semantics MUST be documented before implementation. Each feature
MUST identify its owning boundary and its integration points in the implementation plan.
This keeps the portal maintainable, testable, and adaptable to cloud deployment or provider
changes.

### II. Security, Privacy, and Least Privilege

Authentication, authorization, and data access MUST be enforced at every protected boundary,
not only in the user interface. Role permissions MUST follow least privilege for customers,
support agents, and administrators. Passwords, tokens, secrets, and sensitive AI context MUST
never be logged or exposed in responses. Request ownership MUST be verified for every customer
read or mutation, security-relevant events MUST be audited, and AI-triggered actions MUST require
explicit confirmation and authorization. Security and privacy acceptance scenarios are required
for every feature that handles user, request, chat, or administrative data.

### III. Testable Delivery and Contract Fidelity

Every user story MUST have independently verifiable acceptance scenarios. Changes to API
contracts, authorization, persistence relationships, request lifecycle rules, notifications,
or AI actions MUST include appropriate unit, integration, and contract coverage. Tests MUST
cover successful behavior, validation failures, permission failures, and dependency failures
where applicable. A feature MUST NOT be considered complete until its stated acceptance
scenarios and constitution gates pass.

### IV. Observable, Auditable, and Versioned Operations

Material authentication, authorization, request lifecycle, assignment, configuration,
notification, and AI action events MUST be auditable with actor, object, timestamp, outcome,
and safe contextual data. Operational failures MUST produce actionable structured logs and
user-safe errors without leaking implementation or sensitive data. REST APIs MUST be versioned;
breaking changes MUST be explicitly reviewed, documented, and accompanied by a migration path.
Dashboards, reports, and audit history MUST preserve enough context to explain service outcomes.

### V. Simplicity, Accessibility, and User-Centered Change

The simplest design that satisfies the specification MUST be preferred. New abstractions,
dependencies, and infrastructure MUST have a documented need and a clear ownership boundary.
User workflows MUST provide clear validation, loading, empty, error, and fallback states. The
responsive portal MUST remain usable across supported desktop and mobile viewport sizes, and
important actions MUST be understandable without relying on hidden technical knowledge. AI
assistance MUST complement, never obscure, the direct service-request workflow.

## Security, Architecture, and Product Constraints

- The initial product MUST target Java 21, Spring Boot 3.x, Spring Security with JWT
	authentication, Spring Data JPA, Hibernate, PostgreSQL, Jakarta Validation, and documented
	versioned REST APIs.
- The web client MUST target the latest stable React release available at implementation time,
	TypeScript, Vite, React Router, Axios, responsive design, and an approved component system.
- The AI assistant MUST use a provider-neutral Spring AI-compatible boundary that supports
	OpenAI-compatible and Azure OpenAI-compatible deployments.
- The system MUST support customers, support agents, and administrators with distinct permissions
	and dashboards, and MUST persist request, notification, chat, and audit relationships.
- Local development is the first operating environment. Cloud deployment readiness MUST be
	preserved without coupling core user workflows to a single hosting provider.
- The system MUST apply secure configuration practices, Jakarta validation, global exception
	handling, audit logging, and safe handling of attachments and AI context.
- Feature plans MUST include the relevant API contract, data model, architecture impact,
	acceptance scenarios, operational considerations, and a deployment or configuration note.

## Development Workflow and Quality Gates

- A specification MUST define user value, scope boundaries, actors, acceptance scenarios,
	functional requirements, measurable success criteria, assumptions, and relevant entities.
- A plan MUST include a Constitution Check before research and again after design. Any violation
	MUST be resolved or explicitly justified with a simpler alternative considered and rejected.
- Implementation tasks MUST be organized by independently testable user story, with foundational
	security, persistence, validation, error handling, and observability work identified first.
- Pull requests or equivalent reviews MUST inspect security boundaries, authorization ownership,
	validation, audit coverage, API compatibility, responsive behavior, and test evidence.
- Completion requires passing focused tests and applicable integration or contract tests, updated
	API and data documentation, and verification that no secrets or sensitive request data appear
	in logs, fixtures, screenshots, or documentation.

## Governance

This constitution is the governing quality standard for ServiceHub AI and supersedes conflicting
project guidance. Amendments MUST be proposed with a written rationale, an impact report, and a
review of affected specifications, plans, tasks, tests, and operational documentation. A change
that removes or redefines a principle is a MAJOR version change; a new or materially expanded
principle or section is a MINOR change; wording clarifications and non-semantic corrections are
PATCH changes. The amendment date MUST be updated for every accepted change.

Every feature review MUST record whether the Constitution Check passed, identify any justified
complexity, and confirm that security, testing, observability, versioning, and user-centered
quality gates were addressed. Temporary exceptions MUST name an owner, expiration or review date,
scope, and remediation plan. No exception may weaken authorization or conceal audit-relevant
activity.

**Version**: 1.0.0 | **Ratified**: 2026-09-07 | **Last Amended**: 2026-09-07

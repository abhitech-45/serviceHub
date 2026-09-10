# Data Model

## Core Entities

- `users`: identity, email, password hash, display name, active state, timestamps.
- `roles`: customer, support agent, administrator.
- `user_roles`: many-to-many user-role assignment.
- `service_requests`: reference, subject, description, owner, assignee, category, sub-category, priority, status, resolution notes, timestamps.
- Campus service taxonomy: twelve controlled categories with validated sub-category values for academic, admission, scholarship, library, hostel, sports, transport, IT, examination, placement, finance, and administration support.
- `request_status_history`: request, lifecycle status, actor, remarks, and immutable occurrence timestamp for every status update.
- `request_comments`: request, author, body, visibility, timestamp.
- `request_attachments`: request, uploader, safe storage key, original name, content type, size, scan state.
- `categories`: active taxonomy values with historical display name retained in request snapshots.
- `priorities`: active urgency values with ordering and target metadata.
- `notifications`: recipient, event type, request, channel, delivery state, attempts, timestamps.
- `chat_sessions`: authenticated user, started/closed timestamps, provider metadata without secrets.
- `chat_messages`: session, sender type, safe content, action type, timestamps.
- `audit_logs`: actor, action, entity type/id, outcome, correlation ID, safe context, timestamp.

## Relationship Rules

- A user owns many requests and may be assigned many requests.
- A request has many comments, attachments, notifications, chat action references, and audit events.
- Category and priority changes do not rewrite historical request snapshots.
- Audit logs are append-oriented and not editable by portal users.
- Request status history is append-only; current request status is a projection and never replaces prior lifecycle events.
- Deactivation is preferred over destructive deletion for users and taxonomy referenced by history.

## Migration Order

1. Roles and users.
2. Categories and priorities.
3. Service requests.
4. Request status history, comments, and attachments.
5. Notifications.
6. Chat sessions/messages.
7. Audit logs and indexes.

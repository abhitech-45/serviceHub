# Local Quickstart

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js 22+
- Docker Desktop with PostgreSQL support

## Start dependencies

```powershell
docker compose up -d postgres
```

## Start backend

```powershell
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Backend base URL: `http://localhost:8081/api/v1`

## Start frontend

```powershell
cd frontend
npm install
npm run dev
```

Frontend URL: `http://localhost:5173`

## Verification

```powershell
cd backend
mvn test
cd ..\frontend
npm test
npm run build
```

Health endpoint: `GET http://localhost:8081/actuator/health`
OpenAPI UI: `http://localhost:8081/swagger-ui.html`

## Local administrator

The local profile seeds an administrator account on startup:

- Email: `admin@servicehub.local`
- Password: `Admin@12345`
- Dashboard: `http://localhost:5173/admin`

Set `ADMIN_EMAIL`, `ADMIN_PASSWORD`, and `ADMIN_DISPLAY_NAME` to override these local defaults.

No real secrets belong in source control. Use `.env.example` as the configuration reference and local test adapters for email, attachments, and AI.

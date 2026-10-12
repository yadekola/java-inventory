# Tech365 Java Inventory

Complete Java Spring Boot backend, React frontend and PostgreSQL database

## Features

Create, view, edit and delete products; search names/SKUs; filter by category; paginate results; validate input; reject duplicate SKUs; persist prices and stock quantities. Prices are displayed in NGN and stored as integer kobo.

## Versions

| Component | Version/tag | Location |
|---|---|---|
| Java | 17 | pom.xml  |
| Spring Boot | 3.5.16 | backend/pom.xml |
| Maven | 3.9.11  |  |
| PostgreSQL | 17 |  |
| React / React DOM | 19.2.0 | frontend/package.json |
| Vite | 6.4.1 | frontend/package.json |
| Node.js | 24 | |

Spring JDBC, Flyway, the PostgreSQL driver, validation, Actuator and test dependencies are version-managed by the Spring Boot parent. Run `mvn dependency:tree` for resolved versions. Image tags can receive patch updates; pin approved image digests when immutable builds are required. These are selected versions, not a claim of latest versions.


## Ports

| Service | Container port | Host address | Used inside Docker |
|---|---|---|---|
| web | 5173 | http://localhost:5173 | web:8080 |
| api | 8080 | http://localhost:8080 | api:8080 |
| db | 5432 | Not published by default | db:5432 |


API readiness: **http://localhost:8080/actuator/health/readiness**. Liveness: **http://localhost:8080/actuator/health/liveness**. Actuator is not forwarded by the frontend proxy; access it using the direct API port.

The database healthcheck gates API startup. Web waits for API container creation, not API readiness, so brief 502 responses are possible while Java starts. Wait for the Started log message and a readiness response of UP before using the UI.

## Source structure

- `backend/pom.xml`: dependencies, Java version and executable JAR build.
- `backend/src/main/java/com/tech365/inventory/`: application, controllers, validation, repository and error handling.
- `backend/src/main/resources/application.properties`: environment-driven settings.
- `backend/src/main/resources/db/migration/`: versioned Flyway SQL migrations.
- `backend/src/test/`: API tests with H2 in PostgreSQL compatibility mode.
- `backend`: Maven build stage, then Java runtime stage; output is app.jar.
- `frontend/src/`: React components, API client and styles.
- `docs/API.md`: endpoints, field rules, examples and errors.
- `docs/OPERATIONS.md`: configuration, builds, persistence, backups and troubleshooting.
- `docs/VALIDATION.md`: verification performed and remaining runtime checks.


## Scope

This is a local application without login or per-user permissions. Anyone who can reach the API can modify products. Host ports bind to localhost. The example database user is a PostgreSQL initialization superuser. Add authentication, authorization, TLS, managed secrets, least-privilege database access and operational monitoring before public deployment. Stock quantities are directly editable; this is not an order-processing system or stock movement ledger.

## Quick end-to-end check

After startup, run in PowerShell from the project root:

```powershell
./scripts/smoke.ps1
```

This creates a temporary product through the frontend proxy, reads/updates/searches it, and deletes it. If your PowerShell policy blocks scripts, use the individual API commands in docs/API.md.

## React frontend

The frontend uses React 19 with Vite, not a plain JavaScript page. `frontend/src/main.jsx` contains the application component and state; `frontend/src/api.js` handles API requests. Vite builds static assets. Node.js is needed only during the frontend build or development.


Open the same http://localhost:5173 address. React source edits reload through Vite. Java source edits still require an API rebuild.

For local development, run the Java API on localhost:8080 using docs/OPERATIONS.md. In a second terminal:

```bash
cd frontend
npm ci
npm run dev
```

Open http://localhost:5173. Vite proxies `/api` to localhost:8080. The frontend needs Node.js 24 in this mode. Build independently with `npm run build`.

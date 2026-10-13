# Tech365 Java Inventory — DevOps Project

A full-stack inventory management application built with Spring Boot, React, and PostgreSQL, with Docker Compose for local deployment and GitHub Actions for Continuous Integration (CI).

## Table of Contents

- [1. Project Overview](#1-project-overview)
- [2. Application Features](#2-application-features)
- [3. Technology Stack](#3-technology-stack)
- [4. Architecture](#4-architecture)
- [5. Repository Structure](#5-repository-structure)
- [6. Prerequisites](#6-prerequisites)
- [7. Getting Started](#7-getting-started)
- [8. Running and Verifying the Application](#8-running-and-verifying-the-application)
- [9. Testing and Building](#9-testing-and-building)
- [10. GitHub Actions CI](#10-github-actions-ci)
- [11. Git Workflow](#11-git-workflow)
- [12. Troubleshooting](#12-troubleshooting)
- [13. Security Considerations](#13-security-considerations)
- [14. Stopping the Application](#14-stopping-the-application)
- [15. Current Status and Future Improvements](#15-current-status-and-future-improvements)

## 1. Project Overview

Tech365 Java Inventory is an inventory management application that allows users to manage product records, search inventory, filter products, and work with stock quantities.

The project also demonstrates practical DevOps practices, including:

- Source control with Git and GitHub.
- Containerization with Docker.
- Multi-service orchestration with Docker Compose.
- Environment-based application configuration.
- Automated backend testing.
- Frontend production builds.
- Continuous Integration using GitHub Actions.
- Technical documentation and operational verification.

**Project repository:**
https://github.com/yadekola/java-inventory

## 2. Application Features

The application supports:

- Creating, viewing, editing, and deleting products.
- Searching by product name and SKU.
- Filtering products by category.
- Paginating product results.
- Validating input data.
- Rejecting duplicate SKUs.
- Storing product prices as integer kobo and displaying prices in Nigerian naira (NGN).
- Persisting product data in PostgreSQL.

This project currently has no user login or per-user permissions. See the security section before considering any public deployment.

## 3. Technology Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Backend language |
| Spring Boot | 3.5.16 | Backend application framework |
| Maven | 3.9.11 | Backend build and dependency management |
| PostgreSQL | 17 | Relational database |
| React / React DOM | 19.2.0 | Frontend interface |
| Vite | 6.4.1 | Frontend development and build tool |
| Node.js | 24 | Frontend tooling |
| Docker | Installed locally | Container runtime |
| Docker Compose | Docker Compose plugin | Multi-service orchestration |
| Git | Installed locally | Version control |
| GitHub Actions | Cloud CI | Automated testing and build verification |

Application dependency versions are defined in the project configuration files. Check those files if the project changes.

## 4. Architecture

The application runs as three Docker Compose services.

### Frontend

The React application provides the user interface. Vite serves the frontend and forwards `/api` requests to the backend.

### Backend API

The Spring Boot application handles product operations, validation, and database access. It exposes a REST API and Actuator health endpoints.

### Database

PostgreSQL stores product records. A named Docker volume preserves database files across ordinary container restarts and removal.

### Request flow

1. A user opens the frontend in a browser.
2. The frontend sends a request to an `/api` endpoint.
3. Vite forwards the request to `http://api:8080` inside the Compose network.
4. The backend processes the request and communicates with PostgreSQL at `db:5432`.
5. The API returns a response to the frontend.

### Service ports

| Service | Container port | Host access |
|---|---|---|
| Frontend | 5173 | http://localhost:5173 |
| Backend API | 8080 | http://localhost:8083 |
| PostgreSQL | 5432 | Not published to the host |

The API host port is configurable using `API_PORT`. The default is `8083`.

The frontend and API ports bind to `127.0.0.1`, restricting access through those published ports to the local machine. The services communicate over the Docker Compose network.

## 5. Repository Structure

```text
java-inventory/
├── .github/
│   └── workflows/
│       └── ci.yml
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       └── test/
├── frontend/
│   ├── package.json
│   ├── package-lock.json
│   └── src/
├── docs/
│   └── README.md-backup.md
├── scripts/
│   └── smoke.ps1
├── .env.example
├── .gitignore
├── docker-compose.yaml
├── LICENSE
└── README.md
```

This is a simplified view of the important files.

### Key files

- `docker-compose.yaml`: Defines the database, API, frontend, networking, ports, and health checks.
- `backend/pom.xml`: Defines backend dependencies and Maven build configuration.
- `frontend/package.json`: Defines frontend dependencies and npm scripts.
- `frontend/package-lock.json`: Locks frontend dependency versions.
- `.env.example`: Template for local environment configuration.
- `.gitignore`: Excludes local secrets and generated files from Git.
- `.github/workflows/ci.yml`: Defines automated CI jobs.
- `scripts/smoke.ps1`: Provides an end-to-end smoke test.
- `docs/README.md-backup.md`: Backup of the previous README.

## 6. Prerequisites

Install the following tools before starting:

- Git.
- Docker Desktop with Docker Compose support.
- A GitHub account.
- A terminal such as Git Bash or PowerShell.

Start Docker Desktop before running Docker commands.

Java 17 and Maven are needed if you want to build and test the backend directly on your computer. Node.js 24 and npm are needed if you want to install dependencies or build the frontend directly.

For the initial setup, use Docker Compose to run the application services.

## 7. Getting Started

### Step 1: Clone the repository

Run:

```bash
git clone https://github.com/yadekola/java-inventory.git
cd java-inventory
```

Confirm the working directory and Git status:

```bash
git status
```

### Step 2: Configure environment variables

Create a local environment file from the example:

```bash
cp .env.example .env
```

Open `.env` and set a strong local database password for `POSTGRES_PASSWORD`. Review the other variables and their defaults in `.env.example` and `docker-compose.yaml`.

The Compose configuration requires `POSTGRES_PASSWORD`; it does not silently provide a default password.

**Security:** Never commit a real `.env` file or publish database credentials. Keep `.env.example` free of actual secrets.

### Step 3: Build and start the application

From the project root, run:

```bash
docker compose up --build -d
```

Docker Compose builds the application images where build configurations are provided, creates the services and network, and starts the containers in the background.

### Step 4: Check service status

Run:

```bash
docker compose ps
```

Wait for the database and API health checks to pass and for the frontend to start.

If a service is unhealthy or exits, inspect its logs before proceeding.

## 8. Running and Verifying the Application

### Open the frontend

Visit:

http://localhost:5173

### Check the frontend response

Run:

```bash
curl -I http://localhost:5173
```

An HTTP `200` response indicates that the frontend server is responding successfully.

### Check the product API

Run:

```bash
curl http://localhost:5173/api/products
```

The request uses the frontend proxy to reach the backend. A successful response should contain JSON with pagination information and a product data array. The actual records depend on the database contents.

### Check API health

The API is published on host port `8083`.

Readiness:

```bash
curl http://localhost:8083/actuator/health/readiness
```

Liveness:

```bash
curl http://localhost:8083/actuator/health/liveness
```

The readiness endpoint indicates whether the application is ready to serve requests. The liveness endpoint reports application liveness.

### Inspect application logs

View all service logs:

```bash
docker compose logs
```

Follow logs continuously:

```bash
docker compose logs -f
```

View a specific service:

```bash
docker compose logs -f api
docker compose logs -f frontend
docker compose logs -f db
```

Press `Ctrl+C` to stop following logs. This does not stop the containers.

### Run the end-to-end smoke test

In PowerShell, from the project root, run:

```powershell
./scripts/smoke.ps1
```

The script exercises product operations through the frontend proxy, including creating a temporary product, reading or updating it, searching, and deleting it.

Run the test against a suitable development database because it creates and deletes data. Review the script before running it if you need to understand exactly which requests it sends.

## 9. Testing and Building

### Backend tests

The backend uses Maven. To run the tests directly, execute:

```bash
cd backend
mvn test
```

The test configuration uses an H2 in-memory database in PostgreSQL compatibility mode. This allows the configured tests to run without connecting to the local PostgreSQL container.

Return to the project root when finished:

```bash
cd ..
```

### Frontend production build

Run:

```bash
cd frontend
npm ci
npm run build
```

`npm ci` installs the dependency versions recorded in `package-lock.json`. `npm run build` generates the frontend production assets.

Return to the project root:

```bash
cd ..
```

## 10. GitHub Actions CI

### Purpose

Continuous Integration (CI) automatically runs checks against code pushed to GitHub. This helps detect problems before changes are considered ready for delivery.

The workflow file is:

```text
.github/workflows/ci.yml
```

### Workflow triggers

The workflow runs when:

- Code is pushed to `master`.
- A pull request targets `master`.

### Job 1: Test Java Backend

The workflow:

1. Checks out the repository.
2. Sets up Java 17.
3. Configures Maven dependency caching.
4. Runs the backend tests:

```bash
mvn -B test
```

The job runs from the `backend` directory.

### Job 2: Build React Frontend

The workflow:

1. Checks out the repository.
2. Sets up Node.js 24.
3. Configures npm caching.
4. Installs dependencies using `npm ci`.
5. Runs the production build using `npm run build`.

The job runs from the `frontend` directory.

### Verify a CI run

1. Push a commit to GitHub.
2. Open the repository's Actions tab.
3. Select the latest `Java Inventory CI` run.
4. Open the run details.
5. Confirm that both jobs complete successfully.

**Initial verification:** The first recorded CI run completed successfully in 32 seconds, with a successful workflow status.

View workflow runs:

https://github.com/yadekola/java-inventory/actions/workflows/ci.yml

A successful CI run confirms that the configured tests and build passed. It does not mean the application has been deployed to production.

## 11. Git Workflow

Check the current state:

```bash
git status
```

Review unstaged changes:

```bash
git diff
```

Stage the README:

```bash
git add README.md
```

Review staged changes:

```bash
git diff --cached
```

Commit:

```bash
git commit -m "Improve project documentation"
```

Push to GitHub:

```bash
git push
```

Before committing, inspect staged files and make sure no credentials, local environment files, or unwanted generated files are included.

## 12. Troubleshooting

### Docker is not running

Start Docker Desktop and check:

```bash
docker version
docker compose version
```

### A container is unhealthy or exits

Check service status and logs:

```bash
docker compose ps
docker compose logs api
docker compose logs db
docker compose logs frontend
```

Investigate the first relevant error in the logs.

### Database connection fails

Check that:

- `POSTGRES_PASSWORD` is configured in `.env`.
- The database service is running and healthy.
- The backend uses `DB_HOST=db` and `DB_PORT=5432`.
- Database name and username match the Compose configuration.

### The frontend loads but API requests fail

Check:

```bash
docker compose ps
docker compose logs api
docker compose logs frontend
```

Confirm that the frontend's `API_PROXY_TARGET` is `http://api:8080` in the Compose environment and that the API becomes healthy.

### The API is not ready

The first startup may take time while the application initializes and runs database migrations. Inspect:

```bash
docker compose logs -f api
```

Then retry the readiness endpoint:

```bash
curl http://localhost:8083/actuator/health/readiness
```

### GitHub Actions fails

Open the failed run in GitHub's Actions tab. Select the failed job and inspect the step logs.

For backend failures, examine the Maven test output. For frontend failures, examine `npm ci` and `npm run build`. Correct the underlying issue, verify locally where practical, and push the fix.

### A host port is already in use

Check which process is using the conflicting port or change the host-side port mapping in `docker-compose.yaml`. Update the documented URL if the port changes.

## 13. Security Considerations

This project is intended as a local learning and development application.

Important limitations:

- There is currently no login or per-user authorization.
- Anyone who can reach the API may be able to modify product records.
- The example database user is configured as a PostgreSQL initialization superuser.
- Host ports are bound to localhost in the supplied Compose configuration.
- Local `.env` files contain configuration that must not be committed when they include secrets.

Before deploying publicly, implement and test appropriate authentication and authorization, use a least-privilege database account, secure secrets, configure TLS, and review network exposure, monitoring, backups, and recovery.

Do not assume localhost port binding is a replacement for proper security in a production deployment.

## 14. Stopping the Application

Stop and remove the Compose containers and network:

```bash
docker compose down
```

The named `postgres_data` volume is normally preserved, so database files can remain available for a later startup.

To remove the containers and the database volume:

```bash
docker compose down -v
```

**Warning:** The `-v` option removes named volumes managed by this Compose project and can permanently delete stored database data. Use it only when you intentionally want to reset the database and have preserved any required data.

## 15. Current Status and Future Improvements

### Implemented and verified

- Git repository hosted on GitHub.
- Docker Compose configuration for the frontend, backend, and PostgreSQL.
- Persistent PostgreSQL volume.
- Health checks and service dependencies.
- Frontend API proxy configuration.
- Backend tests configured to use H2.
- GitHub Actions workflow for backend tests and frontend builds.
- Initial CI run completed successfully.
- Local application and API checks performed.

### Potential future improvements

- Build and publish versioned Docker images automatically.
- Publish images to GitHub Container Registry.
- Add image vulnerability and dependency scanning.
- Introduce deployment automation for a suitable hosting environment.
- Configure required CI checks for pull requests.
- Add application monitoring, structured logs, and alerting.
- Document database backup and recovery procedures.
- Add authentication and authorization before public deployment.

These items are future improvements, not claims of completed implementation.

## Learning Outcomes

This project provides hands-on practice with source control, containerization, service networking, environment configuration, automated testing, CI pipelines, troubleshooting, and technical documentation.

The goal is to keep the repository reproducible and make each verified improvement understandable to another developer or DevOps engineer.
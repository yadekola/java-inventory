# Configuration and Operations

This document explains how to configure, run, maintain, and troubleshoot the Java Inventory application.

The project uses Docker Compose to run three services:

* **Frontend:** React application served on port `5173`.
* **API:** Spring Boot application running on port `8080` inside the container and exposed on port `8083` on the host by default.
* **Database:** PostgreSQL 17, accessible to the other services through Docker's internal network.

## 1. Configuration

### Environment variables

The application uses environment variables to configure the database connection and service ports.

| Variable            | Default or example     | Description                                                           |
| ------------------- | ---------------------- | --------------------------------------------------------------------- |
| `POSTGRES_DB`       | `inventory`            | Name of the PostgreSQL database                                       |
| `POSTGRES_USER`     | `inventory`            | PostgreSQL username                                                   |
| `POSTGRES_PASSWORD` | Set in `.env`          | Password used by PostgreSQL and the API                               |
| `API_PORT`          | `8083`                 | Host port used to access the API                                      |
| `DB_HOST`           | `db` in Docker Compose | Database hostname used by the API                                     |
| `DB_PORT`           | `5432`                 | PostgreSQL port used by the API                                       |
| `SERVER_PORT`       | `8080`                 | Spring Boot HTTP port; the application configuration uses port `8080` |
| Frontend port       | `5173`                 | Host port used to access the React application                        |

The current Docker Compose configuration does not use `WEB_PORT`. The frontend port is fixed at `5173`.

### Setting up the environment

The project uses a root-level `.env` file to provide configuration values to Docker Compose.

If `.env` does not exist, create it by copying the example file from the project root:

```bash
cp .env.example .env
```

Open `.env` and set `POSTGRES_PASSWORD` to a suitable local development password. Make sure the password is configured consistently for the database and API.

**Security:** Never commit `.env` or real passwords to Git. Keep `.env.example` limited to safe example values.

Docker Compose reads `.env` for variable substitution. Running Spring Boot directly on your computer is different: Spring Boot does not automatically load the root `.env` file. You must configure the required environment variables separately when using that approach.

## 2. Docker Images and Builds

Docker packages the application and its dependencies into images so that the services can run in consistent environments.

### Backend image

The backend is built using a Java 17 JDK and Maven. The final runtime image uses a Java 17 JRE and runs the packaged application from `/app/app.jar`.

The Docker build is organized to improve caching. Dependency downloads can be reused when application source files change, reducing unnecessary downloads during later builds.

The final application image runs as a non-root user to reduce unnecessary privileges.

### Frontend image

The frontend uses React and Vite. Its production image uses an unprivileged Nginx image to serve the frontend.

The frontend communicates with the API through the configured proxy target. In Docker Compose, this target is `http://api:8080`, where `api` is the Compose service name.

### Rebuilding images

When source code changes, rebuild the affected images so the containers use the updated application.

To rebuild and start the services:

```bash
docker compose up --build -d
```

The default Compose setup does not mount the project source files into the running containers. Changes to source files therefore do not automatically appear in the running application.

## 3. Running the Application

### Start the application

Run these commands from the project root, where `docker-compose.yaml` is located:

```bash
docker compose up --build -d
docker compose ps
```

The first command builds the required images and starts the services in the background. The second displays their current status.

The API waits for PostgreSQL to become healthy before starting. The frontend is configured to wait for the API to become healthy.

### Access the application

| Service                | Address                                         |
| ---------------------- | ----------------------------------------------- |
| Frontend               | http://localhost:5173                           |
| API base URL           | http://localhost:8083/api                       |
| API readiness endpoint | http://localhost:8083/actuator/health/readiness |

The API's internal container port is `8080`. The host port defaults to `8083`, but it can be changed through `API_PORT` in `.env`.

PostgreSQL uses port `5432` inside the Docker network. The current Compose configuration does not publish the database port to the host.

### View application logs

To view the API logs:

```bash
docker compose logs -f api
```

To view the database logs:

```bash
docker compose logs -f db
```

To view the frontend logs:

```bash
docker compose logs -f frontend
```

Press `Ctrl+C` to stop following the logs. This does not stop the containers.

### Stop the application

To stop and remove the containers and the default Compose network:

```bash
docker compose down
```

This command preserves the named PostgreSQL data volume.

**Important:** Do not add `-v` to the command unless you deliberately want to remove the associated volumes and understand the data-loss consequences.

## 4. Running the API Directly on Your Computer

The recommended setup is to run the full application through Docker Compose.

The current Docker Compose configuration does not expose PostgreSQL directly to your computer. To run the API outside Docker, you must first configure a database connection that your computer can access.

If you want to run the API directly, first configure a suitable PostgreSQL connection that is accessible from your computer. Then install JDK 17 and Maven 3.9.x, configure the required database environment variables, and run the following commands from the `backend` directory:

```bash
mvn verify
mvn spring-boot:run
```

With the application's default Spring Boot configuration, the locally running API uses port `8080`. Its base URL is:

http://localhost:8080/api

To run the packaged application instead, build it with:

```bash
mvn package
```

Then run the generated JAR using the actual filename in the `backend/target` directory and the required database environment variables.

The frontend can be run separately for development. From the `frontend` directory, install dependencies and start Vite:

```bash
npm ci
npm run dev
```

Open http://localhost:5173 in your browser.

## 5. Database Migrations and Persistence

### Database migrations

The project uses Flyway to manage database schema changes.

The migration `V1__create_products.sql` creates the initial database schema. Flyway applies pending migrations during application startup.

For future schema changes, create a new migration file, for example:

`V2__add_supplier.sql`

Do not modify migrations that have already been applied to a database. Flyway validates migration checksums, and changing an applied migration can cause validation errors.

Migration history is recorded in the `flyway_schema_history` table.

### Database persistence

PostgreSQL data is stored in the named Docker volume mounted at `/var/lib/postgresql/data`.

This volume allows database data to survive container removal and recreation, provided the volume itself is preserved.

To stop the application without deleting the database data, use:

```bash
docker compose down
```

Avoid removing the volume when troubleshooting unless you intend to delete the stored database data.

### PostgreSQL versions

The current configuration uses PostgreSQL 17. Avoid changing the image to `postgres:latest` without planning the upgrade.

Major PostgreSQL version upgrades require an appropriate migration or backup-and-restore process. Do not assume that an existing database volume can be safely reused with any newer major version.

### Database credentials

PostgreSQL's initialization variables create the database and user credentials when the data directory is initialized for the first time.

Changing `POSTGRES_PASSWORD` in `.env` does not automatically change the password stored in an existing PostgreSQL database.

If the configured password and the database password no longer match, restore the working configuration or perform a deliberate database password rotation. Do not delete the database volume as a shortcut.

## 6. Troubleshooting

| Problem                                      | Possible cause and action                                                                                                               |
| -------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| Frontend does not load                       | Check that the frontend container is running and port `5173` is available                                                               |
| Frontend returns a 502 error                 | Check whether the API has started and inspect its logs                                                                                  |
| API container exits                          | Inspect the API logs for database connection, credential, migration, or startup errors                                                  |
| Database is unhealthy                        | Inspect database logs and verify the configured database credentials                                                                    |
| API cannot be reached                        | Confirm that the API container is running and check the host port configured by `API_PORT`                                              |
| Port is already in use                       | Change `API_PORT` for the API, or stop the process occupying the port; the frontend port is fixed at `5173` in the current Compose file |
| SKU already exists                           | Use a different SKU; SKUs are normalized to uppercase                                                                                   |
| PUT request is rejected                      | Check that all five required editable fields are included                                                                               |
| Price is rejected                            | Send the price as an integer number of kobo, not a decimal amount or numeric string                                                     |
| Database password fails after editing `.env` | The existing database volume may still contain the original password                                                                    |
| Source changes are not visible               | Rebuild the affected image and refresh the browser                                                                                      |
| Local Java cannot connect to PostgreSQL      | The default Compose setup does not publish PostgreSQL to the host; configure an accessible database connection first                    |
| Frontend is opened as a local file           | Open http://localhost:5173 instead so the development server and API proxy can work correctly                                           |
| Maven dependency downloads fail              | Check your internet connection and access to Maven Central                                                                              |

## 7. Useful Official Documentation

* [Spring Boot 3.5 system requirements](https://docs.spring.io/spring-boot/3.5/system-requirements.html)
* [Docker Compose startup order](https://docs.docker.com/compose/how-tos/startup-order/)
* [Docker Compose documentation](https://docs.docker.com/compose/)

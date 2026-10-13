# Validation Record

**Prepared on:** 11 October 2026

## 1. Checks Completed

The following checks completed successfully:

* Java source compiled using JDK 17 and Maven 3.9.11.
* Two Spring Boot API test methods passed with zero failures or errors. They cover product creation, retrieval, full updates, search and filtering, duplicate SKU handling, deletion, missing records, missing fields, invalid pagination, and invalid IDs.
* Flyway successfully applied the SQL migration to the H2 test database.
* The React production build completed successfully using Vite.
* The Docker Compose YAML and `pom.xml` files parsed successfully.

## 2. Test Environment and Limitations

### Database and tests

The test database uses H2 in PostgreSQL compatibility mode. It is not a real PostgreSQL server, so these tests do not confirm integration with PostgreSQL.

The Mockito subclass mock maker is configured in the test resources to avoid requiring dynamic Java agent attachment for these tests.

### Maven packaging

Final executable JAR packaging could not complete because Maven could not resolve three dependency descriptors required by `maven-jar-plugin`: `file-management`, `maven-archiver`, and `plexus-archiver`.

Source compilation and tests completed before the packaging failure. A successful full `mvn verify` run is therefore **not claimed**.

### Docker and runtime validation

Docker was unavailable in the validation environment. The following checks have not been performed:

* Docker image builds and container startup.
* Nginx routing.
* Integration with a real PostgreSQL server.
* Browser-based interaction with the application.
* Data persistence across container restarts.
* Execution of the PowerShell smoke-test script.

The Docker build runs `mvn verify`. Full runtime validation remains necessary on a Docker-enabled machine.

## 3. Runtime Verification

After starting the application with Docker Compose, run the following commands in PowerShell:

```powershell
Invoke-RestMethod http://localhost:8083/actuator/health/readiness
./scripts/smoke.ps1
```

The readiness endpoint should return a healthy response when the application and its required dependencies are ready.

Open http://localhost:5173 in a browser and perform these checks:

1. Add a product.
2. Edit the product.
3. Refresh the page and confirm the product remains visible.
4. Stop the application using `docker compose down`.
5. Start it again and confirm the product is still present.

The smoke-test script should be reviewed for its own cleanup behavior; test scripts create and delete temporary records where indicated.

## 4. Frontend and Backend Status

The Java backend is unchanged from the Java edition whose tests passed. This revision replaces the frontend with React.

The React production build succeeded, but browser-based testing and Docker integration testing were not performed.

## 5. Overall Assessment

**Completed:** Java source compilation, the reported Spring Boot API tests, Flyway migration against H2, React production build, and configuration-file parsing.

**Pending:** Successful executable JAR packaging, Docker-based validation, real PostgreSQL integration, browser testing, persistence testing, and the PowerShell smoke test.

The application should not be considered fully runtime-validated until the pending checks have been completed successfully.
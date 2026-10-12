# Validation record

Prepared on 11 October 2026.

Completed checks:

- Java source compiled using JDK 17 and Maven 3.9.11.
- Two Spring Boot API test methods passed with zero failures/errors. They cover product creation, retrieval, full update, search/filter, duplicate SKU handling, deletion, missing records, missing fields, invalid pagination and invalid IDs.
- Flyway applied the SQL migration to the H2 test database successfully.
- React production build passed using Vite.
- Compose YAML and pom.xml parsed successfully.

The test database is H2 in PostgreSQL compatibility mode, not a real PostgreSQL server. The Mockito subclass mock maker is selected in test resources to avoid requiring dynamic Java agent attachment for these tests.

Final executable JAR packaging could not complete because Maven could not resolve three maven-jar-plugin dependency descriptors (file-management, maven-archiver and plexus-archiver) in this environment. Source compilation and tests completed before that packaging failure. A complete mvn verify success is not claimed.

Docker is not available here. Docker image builds, Nginx routing, real PostgreSQL integration, browser interaction, persistence and the PowerShell smoke script have not been executed here. The included Docker build runs mvn verify; runtime validation is still required on a Docker-enabled machine.

Verification commands after startup:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health/readiness
./scripts/smoke.ps1
```

Open http://localhost:5173, add a product, edit it and refresh the page. Stop with docker compose down, start again, and confirm the product remains. Test scripts create/delete their own temporary records where indicated.

The Java backend is unchanged from the Java edition whose tests passed. This revision replaces the frontend with React. React build succeeded; no browser or Docker integration run was performed.

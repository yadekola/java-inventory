# Configuration and operations

## Variables

| Variable | Default/example | Meaning |
|---|---|---|
| POSTGRES_DB | inventory | Database name |
| POSTGRES_USER | inventory | Database login |
| POSTGRES_PASSWORD | local-java-change-me | Demonstration password |
| WEB_PORT | 5173 | Host frontend port |
| API_PORT | 8080 | Host API port |
| DB_HOST | db in Compose, localhost otherwise | Java database host |
| DB_PORT | 5432 | Java database port |
| SERVER_PORT | 8080 | Spring Boot HTTP port; keep 8080 in provided Compose setup |

Compose reads root .env for substitutions and explicitly passes the three database values to API/db. Spring Boot does not automatically load this .env file when run directly. Set environment variables in your shell for local Java execution. Never commit real passwords.

## Dockerfiles and build

Maven builds and tests the application using a Java 17 JDK. The final image contains only a Java 17 JRE and `/app/app.jar`. `finalName` in pom.xml makes the artifact name predictable. Dependency download is a separate Docker layer, so code edits can reuse cached dependencies. `dependency:go-offline` warms the cache; it is not a guarantee that all subsequent build steps are network-free.

The final image runs as a non-root app user. Nginx uses the unprivileged image. Source files are copied into images, so rebuild after edits. The optional compose.dev.yaml adds frontend source mounts and runs Vite for React live reload. The default setup has no source mounts.

## Local Java API execution

Install JDK 17 and Maven 3.9.x. From the project root, copy .env.example to .env, then start only the database with a localhost port:

PowerShell, in backend:

```powershell
$env:DB_HOST='localhost'
$env:DB_PORT='5434'
$env:POSTGRES_DB='inventory'
$env:POSTGRES_USER='inventory'
$env:POSTGRES_PASSWORD='local-java-change-me'
mvn verify
mvn spring-boot:run
```

Linux/macOS, in backend:

```bash
export DB_HOST=localhost DB_PORT=5434 POSTGRES_DB=inventory POSTGRES_USER=inventory
export POSTGRES_PASSWORD=local-java-change-me
mvn verify
mvn spring-boot:run
```

API address in this mode: http://localhost:8080/api. These commands run the API. Start React separately with npm ci and npm run dev from frontend, then open localhost:5173; see README.md. Match credentials to root .env. Stop local Java before returning to other configurations. To build and run a JAR directly: `mvn package`, then `java -jar target/app.jar` with the same environment variables.

## Migrations and persistence

Flyway applies `V1__create_products.sql` before the API completes startup. Later changes should be new files such as `V2__add_supplier.sql`, followed by a rebuild. Do not edit applied migrations; Flyway validates their checksums. The migration history is stored in flyway_schema_history. This project has no destructive automatic schema recreation.

PostgreSQL 17 data lives in the named volume at /var/lib/postgresql/data. Do not change to postgres:latest against an existing volume. PostgreSQL 18+ changes the default data layout, and major upgrades need a planned upgrade or dump/restore process.

Initialization variables only create credentials/database on the first empty-volume startup. Editing a password in .env does not rotate an existing database password. Preserve the volume and either restore the working setting or rotate the database login deliberately.

## Troubleshooting

| Symptom | Check/action |
|---|---|
| Frontend 502 during startup | Wait for Java Started log and readiness UP |
| API exits | Inspect API logs for credentials or migration failure |
| Port in use | Change WEB_PORT/API_PORT and recreate |
| SKU conflict | Use another code; SKUs normalize to uppercase |
| PUT rejected | Include all five editable fields |
| Price rejected | Send integer kobo, not decimals or numeric strings |
| Password fails after .env edit | Existing volume retains original password |
| Changes invisible | Rebuild images; refresh browser |
| Host API cannot connect | Local DB override publishes port 5434 |
| Frontend opened as a file | Use http://localhost:5173; relative API fetch needs the proxy |
| Maven download failure | Check internet/proxy access to Maven Central |

Official references: https://docs.spring.io/spring-boot/3.5/system-requirements.html and https://docs.docker.com/compose/how-tos/startup-order/.

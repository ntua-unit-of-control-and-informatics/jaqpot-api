# Repository Guidelines

## Project Structure & Module Organization
- `src/main/kotlin/` — Kotlin source (Spring Boot app under `org.jaqpot.api`).
- `src/main/resources/` — config and assets (`application.yml`, `openapi.yaml`, `db/migration` for Flyway).
- `src/test/kotlin/` — unit/integration tests (JUnit 5, Testcontainers).
- Build scripts: `build.gradle.kts`, `settings.gradle`.
- Generated OpenAPI sources: `build/openapi` (added to `main` source set).

## Build, Test, and Development Commands
- `./gradlew build` — generate OpenAPI stubs, compile, test, and package `jaqpot-api.jar`.
- `./gradlew test` — run unit/integration tests (uses JUnit Platform).
- `./gradlew bootRun` — run the API locally (requires services in `application.yml`).
- `./gradlew bootBuildImage` — build an OCI image (`upcintua/jaqpot-api`).
- `./gradlew openApiGenerate` — regenerate server interfaces from `openapi.yaml`.

## Coding Style & Naming Conventions
- Kotlin (JDK 21). Follow Kotlin official style; 4-space indent; braces on same line.
- Packages under `org.jaqpot.api`. Services end with `Service`, controllers/APIs under `service/.../Api`.
- Data transfer objects end with `Dto` (via OpenAPI codegen).
- Prefer Kotlin idioms: null-safety, `data class`, extension functions, coroutines where applicable.

## Testing Guidelines
- Frameworks: JUnit 5, Spring Boot Test, MockK, Testcontainers (PostgreSQL/Keycloak).
- Place tests mirroring package structure; name files `*Test.kt`.
- Run a single test: `./gradlew test --tests "*ModelApiTest"`.
- Add integration tests for new endpoints; include Flyway migrations as needed.

## Commit & Pull Request Guidelines
- Use Conventional Commit style where possible: `feat:`, `fix:`, `deps:`, `ci:`, `refactor:`. Example: `deps(deps): bump spring-boot to 3.5.5`.
- PRs must include: clear summary, linked issue(s), migration notes (if touching `db/migration`), and API changes (update `openapi.yaml`).
- Keep diffs focused; include tests and local run notes.

## Security & Configuration Tips
- Do not commit secrets. Override `application.yml` via env vars (e.g., `SPRING_DATASOURCE_URL`, `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI`).
- Local run expects services at `localhost.jaqpot.org` (DB, Keycloak, etc.); align hosts or update properties.

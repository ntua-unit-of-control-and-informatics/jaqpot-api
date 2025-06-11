# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Development Commands

### Building and Running
- `./gradlew build` - Build the project
- `./gradlew bootRun` - Run the application locally
- `./gradlew test` - Run all tests
- `./gradlew test --tests "ClassName"` - Run specific test class
- `./gradlew test --tests "ClassName.methodName"` - Run specific test method
- `./gradlew bootJar` - Create executable JAR (produces `jaqpot-api.jar`)
- `./gradlew bootBuildImage` - Create Docker image (`upcintua/jaqpot-api`)

### Testing
- Tests use Testcontainers with PostgreSQL and Keycloak
- Integration tests extend `AbstractIntegrationTest` which provides container setup
- Use RestAssured for API testing with predefined RequestSpecification
- MockK for Kotlin mocking
- Test containers automatically start PostgreSQL (alpine3.19) and Keycloak (24.0.3)
- Test realm configuration imported from `/test-realm-export.json`

## Architecture Overview

This is a Spring Boot REST API for machine learning model management and prediction services, built with Kotlin and following OpenAPI-first development.

### OpenAPI-First Development
- API specifications are defined in `src/main/resources/openapi.yaml`
- Controllers are auto-generated from OpenAPI specs using the OpenAPI Generator plugin
- Business logic is implemented in service classes that interface with generated delegates
- The build automatically generates API code before Kotlin compilation (`compileKotlin` depends on `openApiGenerate`)

### Key Service Layer Structure
- **Authentication**: Multi-layered with Keycloak OAuth2, API keys, and email verification
- **Authorization**: Fine-grained permission logic for models, datasets, and organizations
- **Model Management**: Core ML model lifecycle including storage, versioning, and metadata
- **Prediction Services**: Multiple runtime engines (REST, streaming) supporting legacy and modern formats
- **Organization Management**: Multi-tenant organization structure with invitations and user associations

### Database and Migrations
- PostgreSQL with Flyway migrations in `src/main/resources/db/migration/`
- JPA entities in `org.jaqpot.api.entity` package
- Repositories follow Spring Data JPA conventions

### Storage Architecture
- Pluggable storage system: Local filesystem or AWS S3
- Separate buckets for models, datasets, DOAs, preprocessors, and images
- CloudFront integration for image distribution

### Prediction Runtime System
- Multiple prediction engines supporting different model types:
  - **REST Runtimes**: JaqpotPy v6, JaqpotR, Docker-based models
  - **Streaming Runtimes**: Real-time predictions, OpenAI integration
  - **Legacy Runtimes**: Backward compatibility with older model formats
- Runtime selection based on model type and configuration

### Security and Rate Limiting
- Keycloak integration for OAuth2 JWT tokens
- API key authentication system
- Method-level security with custom authorization logic
- Rate limiting with Bucket4j

## Configuration Notes
- Main config in `application.yml` with environment-specific overrides
- Keycloak realm: `jaqpot-local` for development
- Database connection defaults to `localhost.jaqpot.org:5432`
- Multiple service endpoints configured for different runtime environments

## Development Workflow
- API changes require updating `src/main/resources/openapi.yaml` first
- Generated code appears in `build/openapi/` and is automatically included in compilation
- Service implementations use the delegate pattern to interface with generated controllers
- Authorization logic is separated into dedicated classes in `service.authorization` package
- Rate limiting applied via `@WithRateLimitProtectionByUser` annotation

## Key Implementation Patterns
- **Service Layer**: Business logic in `@Service` classes that implement generated API delegates
- **Authorization**: Method-level security with `@PreAuthorize` and custom authorization logic classes
- **Caching**: Strategic caching with `@Cacheable` and custom key generators
- **Storage**: Pluggable storage abstraction supporting local filesystem and S3
- **Error Handling**: Centralized via `ExceptionControllerAdvice` with structured error responses
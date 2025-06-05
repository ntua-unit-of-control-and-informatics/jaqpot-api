[![Build & Test](https://github.com/ntua-unit-of-control-and-informatics/jaqpot-api/actions/workflows/build.yml/badge.svg)](https://github.com/ntua-unit-of-control-and-informatics/jaqpot-api/actions/workflows/build.yml) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ntua-unit-of-control-and-informatics_jaqpot-api&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ntua-unit-of-control-and-informatics_jaqpot-api) [![Publish Docker image](https://github.com/ntua-unit-of-control-and-informatics/jaqpot-api/actions/workflows/publish.yml/badge.svg)](https://github.com/ntua-unit-of-control-and-informatics/jaqpot-api/actions/workflows/publish.yml)

# Jaqpot API

Welcome to the GitHub repository of Jaqpot API! This repository contains the codebase for a modern RESTful API for model
management and prediction services, built using Spring Boot and Kotlin. Supports seamless integration with machine
learning workflows.

## Features

- **Model Management:** Efficient handling of model lifecycle from creation to deployment.
- **Prediction Services:** Robust prediction capabilities to facilitate scientific research and application.
- **Kotlin and Spring Boot:** Utilizes the power and simplicity of Kotlin combined with the robustness of Spring Boot
  for a performant and scalable backend.

## Getting Started

To get started with Jaqpot API, clone this repository and navigate to the project directory:

```bash
git clone https://github.com/ntua-unit-of-control-and-informatics/jaqpot-api
cd jaqpot-api
```

### Prerequisites

Ensure you have the following installed:

- JDK 17 or newer
- Gradle

### Building the Project

Build the project using Gradle:

```bash
gradle build
```

### Running the Server

Run the application locally:

```bash
gradle bootRun
```

### Configuration

When running outside an AWS environment, ensure that the application does not try to use the Web Identity token credentials provider. Set the environment variable `AWS_S3_SKIP_WEB_IDENTITY=true` to force the application to fall back to the default AWS credential chain.

## OpenAPI Specs

In our project, we leverage the OpenAPI Generator to streamline our development process. Here’s how it works:

1. **API Specifications**: All API specifications are defined in the [openapi.yaml](src/main/resources/openapi.yaml)
   file. This file contains the entire API structure and expected behaviors.

2. **Automatic Code Generation**: We use the OpenAPI Generator plugin which automatically picks up specifications from
   the [openapi.yaml](src/main/resources/openapi.yaml) file. The plugin generates all the necessary code scaffolding
   based on these specifications, eliminating the need for manually creating controllers.

3. **Implementing Business Logic**: Developers are required to implement the service that interfaces with the generated
   delegate. This service layer is where the business logic is written, ensuring that it conforms to the API
   specifications defined in the YAML file.

4. **Consistency and Compliance**: By adopting this workflow, we ensure that our API always remains consistent with the
   OpenAPI specifications. It also adheres to the standards set by our internal use of
   the [Stoplight Studio editor](https://upcintua.stoplight.io/studio/jaqpot?importFiles=true), which aids in
   maintaining high standards and compliance throughout the API lifecycle.

Using this approach, we keep our API definitions centralized, up-to-date, and aligned with the overall architectural
requirements.





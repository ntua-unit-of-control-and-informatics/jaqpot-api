plugins {
    val kotlinVersion = "1.9.24"

    java
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.jetbrains.kotlin.jvm") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.spring") version "1.9.23"

    id("org.openapi.generator") version "7.5.0"

    id("org.flywaydb.flyway") version "10.13.0"
}

group = "org.jaqpot"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // spring boot dependencies
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // swagger needed dependencies
    implementation("io.swagger.core.v3:swagger-annotations:2.2.19")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

    // lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // flyway
    implementation("org.flywaydb:flyway-core:10.12.0")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:10.12.0")

    runtimeOnly("org.postgresql:postgresql")
}

springBoot {
    mainClass.set("org.jaqpot.api.JaqpotApiApplicationKt")
}

tasks.compileKotlin {
    dependsOn(tasks.openApiGenerate)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set("$projectDir/src/main/resources/api-swagger.yaml")
    invokerPackage.set("org.jaqpot.api")
    apiPackage.set("org.jaqpot.api")
    modelPackage.set("org.jaqpot.api.model")
    outputDir.set("${buildDir}/openapi")
    modelNameSuffix.set("Dto")
    // config options: https://openapi-generator.tech/docs/generators/kotlin-spring/
    configOptions.set(
        mapOf(
            "dateLibrary" to "java8",
            "library" to "spring-boot",
            "useBeanValidation" to "tr+ue",
            "useTags" to "true",
            "delegatePattern" to "true",
            "useSpringBoot3" to "true"
        )
    )
}

sourceSets {
    main {
        kotlin {
            srcDir("${buildDir}/openapi")
        }
    }
}



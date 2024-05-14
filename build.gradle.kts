plugins {
    java
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.jetbrains.kotlin.jvm") version "1.9.23"
    id("org.jetbrains.kotlin.plugin.spring") version "1.9.23"

    id("org.openapi.generator") version "7.5.0"
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
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.21")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.compileKotlin {
    dependsOn(tasks.openApiGenerate)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("$projectDir/src/main/resources/api-swagger.yaml")
    outputDir.set("$buildDir/generated/openapi")
    apiPackage.set("org.jaqpot.api.controller")
    modelPackage.set("org.jaqpot.api.entity")
    modelNamePrefix.set("Dto")
    configOptions.set(
        mapOf(
            "dateLibrary" to "java8",
            "hideGenerationTimestamp" to "true",
            "interfaceOnly" to "true",
            "library" to "spring-boot",
            "serializableModel" to "true",
            "useBeanValidation" to "true",
            "useTags" to "true",
            "implicitHeaders" to "true",
            "openApiNullable" to "false",
            "delegatePattern" to "true",
            "useSwaggerUI" to "true"
        )
    )
}

sourceSets {
    main {
        kotlin {
            srcDirs("$buildDir/generated/openapi/src/main/java")
        }
    }
}

import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

group = "org.jaqpot"
version = "{{VERSION_PLACEHOLDER}}"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}


plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.openapi.generator)
    alias(libs.plugins.flyway)
}

// Rest of your build file configuration remains the same until dependencies

dependencies {
    // Spring Boot dependencies
    implementation(libs.spring.boot.starter.oauth2.resource.server)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.mail)
    implementation(libs.spring.boot.starter.cache)
    implementation(libs.spring.boot.starter.webflux)

    // Cache
    implementation(libs.caffeine)

    // Prometheus
    implementation(libs.micrometer.prometheus)

    // Freemarker
    implementation(libs.freemarker)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)

    annotationProcessor(libs.spring.boot.configuration.processor)
    developmentOnly(libs.spring.boot.devtools)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.spring.boot.testcontainers)

    // Spring Boot Gradle Plugin
    implementation(libs.spring.boot.gradle.plugin)

    // Logging
    implementation(libs.slf4j.api)
    implementation(libs.kotlin.logging)
    implementation(libs.logback.json.classic)
    implementation(libs.logback.jackson)
    implementation(libs.janino)

    implementation(libs.jackson.kotlin)
    implementation(libs.kotlin.reflect)

    // Swagger
    implementation(libs.springdoc.openapi)

    // Lombok
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // Flyway
    implementation(libs.flyway.core)
    runtimeOnly(libs.flyway.postgresql)

    // Keycloak
    implementation(libs.keycloak.admin)

    // Bucket4j
    implementation(libs.bucket4j)

    // Apache CSV
    implementation(libs.commons.csv)

    runtimeOnly(libs.postgresql)

    // AWS
    implementation(platform(libs.aws.bom))
    implementation(libs.aws.s3)
    implementation(libs.aws.sts)

    // OpenAI
    implementation(libs.openai.client)
    runtimeOnly(libs.ktor.client.okhttp)

    // Testing
    testImplementation(libs.rest.assured)
    testImplementation(libs.rest.assured.kotlin)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.keycloak)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.mockk)
}


springBoot {
    mainClass.set("org.jaqpot.api.JaqpotApiApplicationKt")
}

tasks.compileKotlin {
    dependsOn(tasks.openApiGenerate)
}

tasks.bootJar {
    archiveFileName.set("jaqpot-api.jar")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set("$projectDir/src/main/resources/openapi.yaml")
    invokerPackage.set("org.jaqpot.api")
    apiPackage.set("org.jaqpot.api")
    modelPackage.set("org.jaqpot.api.model")
    outputDir.set("${buildDir}/openapi")
    modelNameSuffix.set("Dto")
    ignoreFileOverride.set("$projectDir/.openapi-generator-ignore")
    // config options: https://openapi-generator.tech/docs/generators/kotlin-spring/
    configOptions.set(
        mapOf(
            "library" to "spring-boot",
            "useBeanValidation" to "true",
            "useTags" to "true",
            "delegatePattern" to "true",
            "useSpringBoot3" to "true"
        )
    )
}

// Dockerize https://docs.spring.io/spring-boot/gradle-plugin/packaging-oci-image.html
tasks.named<BootBuildImage>("bootBuildImage") {
    environment.set(
        mapOf("BP_JVM_VERSION" to "21")
    )
    imageName = "upcintua/jaqpot-api"
}

sourceSets {
    main {
        kotlin {
            srcDir("${buildDir}/openapi")
        }
    }
}



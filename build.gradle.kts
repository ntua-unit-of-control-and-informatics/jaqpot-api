import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    val kotlinVersion = "1.9.24"

    java
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.jetbrains.kotlin.jvm") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.spring") version "1.9.23"

    id("org.openapi.generator") version "7.9.0"

    id("org.flywaydb.flyway") version "10.13.0"
}

group = "org.jaqpot"
version = "{{VERSION_PLACEHOLDER}}"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
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
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-cache")

    // cache
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")

    // prometheus
    implementation("io.micrometer:micrometer-registry-prometheus")

    // freemarker
    implementation("org.freemarker:freemarker:2.3.33")

    // coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.9.0")

    // map hibernate types to kotlin types
    implementation("io.hypersistence:hypersistence-utils-hibernate-63:3.8.3")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")

    // spring boot gradle plugin for dockerizing the app
    implementation("org.springframework.boot:spring-boot-gradle-plugin:3.3.0")

    // logger
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("io.github.oshai:kotlin-logging-jvm:6.0.9")
    // json logs for loki and promtail
    implementation("ch.qos.logback.contrib:logback-json-classic:0.1.5")
    implementation("ch.qos.logback.contrib:logback-jackson:0.1.5")
    implementation("org.codehaus.janino:janino:3.1.12")

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

    // keycloak admin client
    implementation("org.keycloak:keycloak-admin-client:24.0.5")

    // bucket4j for rate limiting
    implementation("com.bucket4j:bucket4j-core:8.10.1")

    // apache csv parser
    implementation("org.apache.commons:commons-csv:1.11.0")

    runtimeOnly("org.postgresql:postgresql")

    // aws
    implementation(platform("software.amazon.awssdk:bom:2.21.1"))
    // With the bom declared, you specify individual SDK dependencies without a version.
    implementation("software.amazon.awssdk:s3")
    implementation("software.amazon.awssdk:sts")

    // tests
    // rest assured
    testImplementation("io.rest-assured:rest-assured:5.4.0")
    testImplementation("io.rest-assured:kotlin-extensions:5.4.0")
    // testcontainers
    testImplementation("org.testcontainers:postgresql:1.19.8")
    testImplementation("com.github.dasniko:testcontainers-keycloak:3.3.1")
    testImplementation("org.testcontainers:junit-jupiter:1.19.8")
    // mockK
    testImplementation("io.mockk:mockk:1.13.11")

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
        mapOf("BP_JVM_VERSION" to "17")
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



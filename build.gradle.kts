import org.jetbrains.kotlin.gradle.targets.js.npm.SemVer.Companion.from

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.2.21"
    id("org.jetbrains.kotlin.plugin.spring") version "2.2.21"
    id("org.springframework.boot") version "3.5.9"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jetbrains.kotlin.plugin.jpa") version "2.2.21"
}

val springAiVersion = "1.1.2"

group = "b1a4"
version = "0.0.1-SNAPSHOT"
description = "harudew-api"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }

}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-web")
    // JWT 토큰 처리를 위한 라이브러리
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.ai:spring-ai-starter-model-bedrock-converse")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("com.mysql:mysql-connector-j")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    implementation("io.qdrant:client:1.16.2")
    // 비동기 테스트
    testImplementation("org.awaitility:awaitility-kotlin:4.2.0")
    // aws s3 (Spring Cloud AWS for Spring Boot 3.x)
    implementation("io.awspring.cloud:spring-cloud-aws-starter-s3:3.3.1")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:$springAiVersion")
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

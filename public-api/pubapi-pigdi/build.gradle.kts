plugins {
    application
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("plugin.spring") version "2.1.0"
}

repositories {
    mavenCentral()
}

configurations.all {
    exclude(group = "gg.scala.commons")
    exclude(group = "gg.scala.store")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")

    // OpenAPI / Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // Caching
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")

    // JSON
    implementation("com.google.code.gson:gson:2.10.1")
}

application {
    mainClass.set("mc.arch.pubapi.pigdi.PigdiApplicationKt")
}

springBoot {
    mainClass.set("mc.arch.pubapi.pigdi.PigdiApplicationKt")
}

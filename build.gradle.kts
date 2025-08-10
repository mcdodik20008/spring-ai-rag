import kotlinx.kover.features.jvm.AggregationType
import kotlinx.kover.features.jvm.CoverageUnit
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType

plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
    // Линтеры/форматирование
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
    id("com.diffplug.spotless") version "7.2.1"
    id("org.jetbrains.kotlinx.kover") version "0.9.1"
}

group = "mcdodik"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.spring.io/milestone") }
}

extra["springAiVersion"] = "1.0.1"

configurations.all {
    exclude(group = "org.springframework", module = "spring-webmvc")
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    implementation(platform(libs.spring.ai.bom))

    // ──────────── CORE ────────────
    implementation(libs.bundles.core)

    // ──────────── Security ────────────
    implementation(libs.bundles.security)

    // ──────────── Trace ────────────
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.zipkin.reporter2:zipkin-reporter-brave")

    // ──────────── Swagger ────────────
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.8.9")

    // ──────────── MyBatis ────────────
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3")

    // ──────────── Kotlin + Coroutines ────────────
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // ──────────── Spring AI ────────────
    implementation(libs.bundles.springai)

    // ──────────── Logging + MDC ────────────
    implementation("ch.qos.logback:logback-classic") // обычно уже есть

    // ──────────── Rate limiting ────────────
    implementation("com.bucket4j:bucket4j-core:8.10.1")

    // ──────────── Jackson ────────────
    implementation(libs.bundles.jackson)

    // ──────────── Markdown, PDF, Tika ────────────
    implementation(libs.bundles.docs)

    // ──────────── Utils ────────────
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // ──────────── Dev / Test ────────────
    implementation(libs.bundles.test)
    implementation(libs.bundles.testRuntime)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

detekt {
    toolVersion = "1.23.6"
    config.setFrom(files("$rootDir/detekt.yml"))
    buildUponDefaultConfig = true
    ignoreFailures = true
    autoCorrect = false
    // исключаем пути целиком
    source.setFrom(
        files(
            "src/main/kotlin",
            "src/test/kotlin",
        ),
    )
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("FAILED", "SKIPPED", "STANDARD_ERROR")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

tasks.register("installGitHook", Copy::class) {
    from("$rootDir/ci/git/pre-push")
    into("$rootDir/.git/hooks")
    fileMode = 0b111101101
}

tasks.named("build") {
    dependsOn("installGitHook")
}

tasks.register("coverage") {
    dependsOn("koverXmlReport", "koverHtmlReport", "koverVerify")
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "**.config.*",
                    "**.configuration.*",
                    "**.dto.*",
                    "**.generated.*",
                )
            }
        }

        total {
            html { onCheck.set(true) }
            xml { onCheck.set(true) }
            verify {
                // 1) По проекту: минимум 80% покрытие строк
                rule {
                    CoverageUnit.LINE
                    AggregationType.COVERED_PERCENTAGE
                    GroupingEntityType.APPLICATION
                    minBound(0) // 80)
                }
                // 2) По каждому классу: минимум 60% строк
                rule {
                    CoverageUnit.LINE
                    AggregationType.COVERED_PERCENTAGE
                    GroupingEntityType.CLASS
                    minBound(0) // 60)
                }
            }
        }
    }

    // Точная настройка инструментации (если какие-то классы ломаются)
    currentProject {
        instrumentation {
            // отключить инструментирование проблемных классов
            // excludedClasses.add("com.example.UnInstrumented*")
            // отключить для всех тестов (если нужно для перф-тестов)
            // disableForAll = true
            // или выборочно:
            // disableForTestTasks.add("nightlyLoadTest")
        }
    }
}

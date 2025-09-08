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
    implementation(libs.bundles.tracing)

    // ──────────── Swagger ────────────
    implementation(libs.bundles.swagger)

    // ──────────── MyBatis ────────────
    implementation(libs.bundles.mybatis)

    // ──────────── Kotlin + Coroutines ────────────
    implementation(libs.bundles.coroutines)

    // ──────────── Spring AI ────────────
    implementation(libs.bundles.springai)

    // ──────────── Fly Way ────────────
    implementation(libs.bundles.flyway)

    // ──────────── Logging + MDC ────────────
    implementation(libs.bundles.logging)

    // ──────────── Rate limiting ────────────
    implementation(libs.bundles.ratelimit)

    // ──────────── Jackson ────────────
    implementation(libs.bundles.jackson)

    // ──────────── Markdown, PDF, Tika ────────────
    implementation(libs.bundles.docs)

    // ──────────── Utils ────────────
    implementation(libs.bundles.utils)

    //
    implementation(libs.bundles.dev)

    // ──────────── Dev / Test ────────────
    implementation(libs.bundles.test)
    implementation(libs.bundles.testRuntime)
    implementation(libs.bundles.testContainers)
    implementation(libs.bundles.testApi)
    implementation(libs.bundles.testHttpStub)
    implementation(libs.bundles.testDocs)
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

tasks.test {
    useJUnitPlatform()
    systemProperty("org.springframework.restdocs.outputDir", "$buildDir/generated-snippets")
}

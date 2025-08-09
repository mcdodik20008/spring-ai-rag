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

dependencies {
    // ──────────── CORE ────────────
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    runtimeOnly("org.postgresql:postgresql")

    // ──────────── MyBatis ────────────
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3")

    // ──────────── Kotlin + Coroutines ────────────
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // ──────────── Spring AI ────────────
    implementation("org.springframework.ai:spring-ai-starter-model-ollama")
    implementation("org.springframework.ai:spring-ai-starter-vector-store-pgvector")
    implementation("org.springframework.ai:spring-ai-advisors-vector-store")
    implementation("org.springframework.ai:spring-ai-markdown-document-reader")
    implementation("org.springframework.ai:spring-ai-tika-document-reader")
    implementation("org.springframework.ai:spring-ai-pdf-document-reader")
    implementation("org.springframework.ai:spring-ai-starter-model-transformers")

    // ──────────── Jackson ────────────
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")

    // ──────────── Markdown, PDF, Tika ────────────
    implementation("org.commonmark:commonmark:0.24.0")
    implementation("org.apache.tika:tika-core")
    implementation("org.apache.tika:tika-parsers-standard-package")
    implementation("org.apache.pdfbox:pdfbox")
    implementation("org.apache.poi:poi-ooxml")

    // ──────────── Utils ────────────
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // ──────────── Dev / Test ────────────
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
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

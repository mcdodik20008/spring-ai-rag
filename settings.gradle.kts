pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo1.maven.org/maven2")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://repo.spring.io/milestone")
    }
}

rootProject.name = "spring-ai"

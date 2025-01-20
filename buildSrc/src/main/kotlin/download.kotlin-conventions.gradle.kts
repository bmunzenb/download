plugins {
    id("org.jetbrains.kotlin.jvm")
    id("io.gitlab.arturbosch.detekt")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

group = "com.munzenberger"
version = "1.0"

base {
    archivesName = "${rootProject.name}-${project.name}"
}

dependencyLocking {
    lockAllConfigurations()
}

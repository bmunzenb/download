plugins {
    id("org.jetbrains.kotlin.jvm")
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

ktlint {
    version.set("1.7.1")
}

group = "com.munzenberger"
version = "1.4"

base {
    archivesName = "${rootProject.name}-${project.name}"
}

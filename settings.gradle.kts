pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "download"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include("core")
include("app")

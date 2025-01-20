plugins {
    id("download.kotlin-conventions")
    application
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("com.munzenberger.download.app.MainKt")
    applicationName = "download"
}

dependencies {
    implementation(project(":core"))
    implementation(libs.clikt)
}

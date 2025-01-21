plugins {
    id("download.kotlin-conventions")
    `java-library`
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.okio)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to "${rootProject.name}-${project.name}",
            "Implementation-Version" to project.version,
        )
    }
}

java {
    withSourcesJar()
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/bmunzenb/download")
            credentials {
                username = System.getenv("GITHUB_USER")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            artifactId = "${rootProject.name}-${project.name}"

            pom {
                name = "Download Core"
                description = "Mass download utilities"
                url = "https://github.com/bmunzenb/download"
                licenses {
                    license {
                        name = "MIT License"
                        url = "https://opensource.org/license/mit"
                    }
                }
                scm {
                    url = "https://github.com/bmunzenb/download"
                }
            }

            from(components["java"])
        }
    }
}

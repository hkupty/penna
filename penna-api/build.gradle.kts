plugins {
    `java-library`
    `maven-publish`
    `signing`
    `pmd`

    id("penna.build.projectVersion")
}

group = "com.hkupty.penna"

repositories {
    mavenCentral()
}

java {
    withJavadocJar()
    withSourcesJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

pmd {
    isConsoleOutput = true
    toolVersion = "7.15.0"
}

// Reproducible builds
tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

// The API is an optional client-facing library and therefore it should not have gaps
// in the documentation, nor fail on linting, so it is easier for consumers to understand
// what it is doing under the hood.
tasks.compileJava {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(
        listOf(
            "-Xlint:all",
            "-Xdoclint:all/public",
            "-Werror",
        ),
    )
}

dependencies {
    compileOnly(libs.slf4j)
    compileOnly(libs.jetbrains.annotations)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Version" to project.version,
            "Implementation-Title" to project.name,
        )
    }
}

publishing {
    publications {
        create<MavenPublication>("penna-api") {
            groupId = "${project.group}"
            artifactId = project.name
            version = "${project.version}"

            pom {
                name = project.name
                description = "An opinionated slf4j backend for structured logging"
                url = "https://github.com/hkupty/penna"
                licenses {
                    license {
                        name = "MIT License"
                        url = "https://github.com/hkupty/penna/blob/main/LICENSE"
                    }
                }
                developers {
                    developer {
                        id = "hkupty"
                        name = "Henry John Kupty"
                        email = "hkupty@gmail.com"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/hkupty/penna"
                    developerConnection = "scm:git:ssh://github.com/hkupty/penna"
                    url = "https://github.com/hkupty/penna"
                }
            }
        }
    }
}

// signing {
//     sign configurations.archives
//     sign publishing.publications.mavenJava
// }

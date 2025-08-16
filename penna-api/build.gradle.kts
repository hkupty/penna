plugins {
    `java-library`

    pmd
    signing

    alias(libs.plugins.publish)
    id("penna.build.projectVersion")
}

group = "com.hkupty.penna"

repositories {
    mavenCentral()
}

java {
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

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates("com.hkupty.penna", "penna-api", "${project.version}")

    pom {
        name = project.name
        description = "An opinionated slf4j backend for structured logging"
        inceptionYear = "2023"
        url = "https://github.com/hkupty/penna"
        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/license/mit"
                distribution = "https://github.com/hkupty/penna/blob/dev/0.9/LICENSE"
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

signing {
    useGpgCmd()
    sign(publishing.publications)
}

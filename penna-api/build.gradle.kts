plugins {
    `java-library`
    id("penna.publishing")
    id("penna.build.projectVersion")
    pmd
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

// version = "0.8.1"

dependencies {
    compileOnly(libs.slf4j)
    compileOnly(libs.jetbrains.annotations)

    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes("Implementation-Version" to project.version)
    }
}

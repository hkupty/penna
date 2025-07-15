plugins {
    `java-library`

    signing
    pmd

    alias(libs.plugins.publish)
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

    sourceSets = listOf(java.sourceSets["main"])

    ruleSets("category/java/performance.xml", "category/java/bestpractices.xml")
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
            // "-Xdoclint:all/public",
            "-Werror",
        ),
    )
}

dependencies {
    implementation(project(":penna-api"))
    implementation(project(":penna-core"))
    implementation(libs.slf4j)

    // (optional) Jackson support
    compileOnly(libs.jackson.core)
    compileOnly(libs.jackson.databind)
    compileOnly(libs.jackson.yaml)

    // Annotations for better code readability
    compileOnly(libs.jetbrains.annotations)

    // (optional) SnakeYaml support
    compileOnly(libs.snakeyaml.plain)
    compileOnly(libs.snakeyaml.engine)

    // Tests
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.pioneer)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.jackson.core)
    testRuntimeOnly(libs.jackson.databind)
    testRuntimeOnly(libs.jackson.yaml)
    testRuntimeOnly(libs.snakeyaml.engine)

    testImplementation(libs.jackson.core)
    testImplementation(libs.jackson.databind)
    testImplementation(libs.jackson.yaml)
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

    coordinates("com.hkupty.penna", "penna-yaml-config", "${project.version}")

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

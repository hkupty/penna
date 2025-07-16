plugins {
    `java-library`
    `jvm-test-suite`

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

    ruleSetFiles("pmd/ruleset.xml")
}

// Reproducible builds
tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.compileJava {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(
        listOf(
            "-Xlint:all",
            // "-Xdoclint:all/public",
            // "-Werror",
        ),
    )
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }

        register<JvmTestSuite>("propertyTesting") {
            useJUnitJupiter()

            dependencies {
                implementation(project(":penna-api"))
                implementation(project())

                implementation(libs.slf4j)
                implementation(libs.jqwik)
                implementation(libs.commons.math)
                implementation(libs.jackson.core)
                implementation(libs.jackson.databind)

                compileOnly(libs.jetbrains.annotations)
            }

            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                    }
                }
            }
        }
    }
}

dependencies {
    implementation(project(":penna-api"))
    implementation(libs.slf4j)

    compileOnly(libs.jetbrains.annotations)

    testImplementation(libs.junit.pioneer)

    testRuntimeOnly(libs.junit.engine)
    testImplementation(libs.junit.api)
    testImplementation(libs.jackson.core)
    testImplementation(libs.jackson.databind)
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Version" to project.version,
            "Implementation-Title" to project.name,
        )
    }
}

tasks.named("check") {
    dependsOn(testing.suites.named("propertyTesting"))
}


mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates("com.hkupty.penna", "penna-core", "${project.version}")

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

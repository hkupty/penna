pluginManagement {
    includeBuild("build-logic")

    repositories {
        gradlePluginPortal()
    }
}

buildscript {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "penna"
include(
    "penna-api",
    "penna-core",
    "penna-integration",
    "penna-yaml-config",
    "sample",
    // , "penna-perf"
)

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("projectVersionPlugin") {
            id = "penna.build.projectVersion"
            implementationClass = "ProjectVersionPlugin"
        }
    }
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())
}

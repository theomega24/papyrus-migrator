plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.omega24"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.h2database", "h2", "2.1.214")

    implementation("com.fasterxml.jackson.core", "jackson-core", "2.15.1")
    implementation("com.fasterxml.jackson.core", "jackson-annotations", "2.15.1")
    implementation("com.fasterxml.jackson.core", "jackson-databind", "2.15.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "dev.omega24.Main"
        }
    }
}

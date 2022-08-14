import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object Versions {
    const val project = "1.0.0"
}

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    `maven-publish`
}

group = "ilio"
version = Versions.project

repositories {
    mavenLocal()
    maven(url="https://jitpack.io")
    mavenCentral()
}

dependencies {
    implementation("org.apache.httpcomponents.client5:httpclient5:5.1.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0-RC")

    testImplementation("io.kotest:kotest-runner-junit5:5.4.2")
    testImplementation("io.mockk:mockk:1.12.5")
//    testImplementation("io.kotest:kotest-assertions-core:5.4.2")
//    testImplementation("io.kotest:kotest-property:5.4.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "ilio"
            artifactId = "bark-java-client"
            version = Versions.project

            from(components["kotlin"])
        }
    }
}

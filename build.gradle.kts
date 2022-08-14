import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
}

group = "ilio"
version = "1.0.0"

repositories {
    mavenLocal()
    maven(url="https://jitpack.io")
    mavenCentral()
}

dependencies {
    implementation("org.apache.httpcomponents.client5:httpclient5:5.1.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0-RC")

//    testImplementation(platform("org.junit:junit-bom:5.9.0"))
//    testImplementation("org.junit.jupiter:junit-jupiter")
//    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5:5.4.2")
//    testImplementation("io.kotest:kotest-assertions-core:5.4.2")
//    testImplementation("io.kotest:kotest-property:5.4.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
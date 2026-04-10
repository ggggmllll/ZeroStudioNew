plugins {
    id("maven-publish")
    kotlin("jvm")
    alias(libs.plugins.protobuf)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    // dependencies are constrained to versions defined
    // in /platform/build.gradle.kts
    // implementation(platform(project(":platform")))

    implementation(kotlin("stdlib"))
    implementation(libs.google.gson)
    implementation(libs.org.jetbrains.exposed.core)
    implementation(libs.org.jetbrains.exposed.dao)
    implementation(libs.google.protobuf.java)
    implementation(libs.com.google.protobuf.java.util)

    testImplementation(libs.hamcrest.all)
    testImplementation(libs.tests.junit)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.18.2"
    }
}

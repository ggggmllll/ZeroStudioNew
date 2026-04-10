plugins {
    id("maven-publish")
    kotlin("jvm")
    alias(libs.plugins.com.google.protobuf)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    @Suppress("UnstableApiUsage")
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(11)
        vendor = JvmVendorSpec.AZUL
    }
}


dependencies {
    // dependencies are constrained to versions defined
    // in /platform/build.gradle.kts
    implementation(platform(project(":platform")))

    implementation(kotlin("stdlib"))
    implementation(libs.com.google.code.gson)
    implementation(libs.org.jetbrains.exposed.core)
    implementation(libs.org.jetbrains.exposed.dao)
    implementation(libs.com.google.protobuf.java)
    implementation(libs.com.google.protobuf.java.util)

    testImplementation(libs.hamcrest.all)
    testImplementation(libs.junit.junit)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.18.2"
    }
}

plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "me.rerere.rikkahub.web"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)

    // ktor server
    implementation(libs.io.ktor.server.default.headers)
    implementation(libs.io.ktor.server.conditional.headers)
    implementation(libs.io.ktor.server.compression)
    implementation(libs.io.ktor.server.cors)
    api(libs.io.ktor.server.auth)
    api(libs.io.ktor.server.auth.jwt)
    api(libs.io.ktor.server.core)
    implementation(libs.io.ktor.server.host.common)
    api(libs.io.ktor.server.content.negotiation)
    api(libs.io.ktor.server.status.pages)
    api(libs.io.ktor.server.sse)
    api(libs.io.ktor.server.cio)

    testImplementation(libs.tests.junit)
    androidTestImplementation(libs.tests.androidx.junit)
    androidTestImplementation(libs.tests.androidx.espresso.core)
}
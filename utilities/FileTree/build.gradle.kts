group = "com.github.RohitKushvaha01"

version = "1.0.0"

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  `maven-publish`
}

android {
  namespace = "com.rk.filetree"
  compileSdk = 34

  defaultConfig {
    minSdk = 24

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  compilerOptions {
    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
}

}

publishing {
  publications {
    create<MavenPublication>("maven") {
      // Use the correct component for Android libraries (usually "release" or "debug")
      from(components.findByName("release"))
    }
  }
  repositories { maven { url = uri("https://jitpack.io") } }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.google.material) 
  
  testImplementation(libs.tests.junit) 
  androidTestImplementation(libs.androidx.junit) 
  androidTestImplementation(libs.androidx.espresso.core)
}

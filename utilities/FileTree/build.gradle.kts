import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
  androidTestImplementation(libs.tests.androidx.junit) 
  androidTestImplementation(libs.tests.androidx.espresso.core)
}

kotlin { jvmToolchain(11) }

tasks.withType<KotlinCompile>().configureEach {
  compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
}

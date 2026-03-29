plugins {
  id("com.android.library")
  id("kotlin-android")
}

android {
  namespace = "com.termux"
  }
}

dependencies {
  implementation(projects.core.common)
  implementation(projects.core.resources)
  implementation(projects.termux.shared)
  implementation(projects.termux.emulator)
}


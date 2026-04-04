import com.itsaky.androidide.build.config.BuildConfig

plugins {
  id("com.android.library")
  id("kotlin-android")
}

android {
  namespace = "${BuildConfig.packageName}.lsp.clangd"
  compileSdk = 36

  defaultConfig {
    minSdk = 28

    externalNativeBuild {
      cmake {
        cppFlags += listOf("-std=c++17", "-fexceptions", "-frtti")
        arguments += listOf("-DANDROID_STL=c++_shared")
      }
    }

    ndk { abiFilters += listOf("arm64-v8a", "x86_64") }
  }

  externalNativeBuild {
    cmake {
      path = file("src/main/cpp/CMakeLists.txt")
      version = "3.22.1"
    }
  }

  buildFeatures { buildConfig = true }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  packaging {
    jniLibs {
      useLegacyPackaging = true
      excludes += setOf("**/libc++_shared.so")
    }
  }
}

dependencies {
  implementation(projects.core.lspApi)
  implementation(projects.core.lspModels)
  implementation(projects.core.projects)
  implementation(projects.editor.api)
  implementation(projects.editor.editorLsp)
  implementation(projects.editor.impl)

  implementation(libs.common.kotlin)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.preference)
}

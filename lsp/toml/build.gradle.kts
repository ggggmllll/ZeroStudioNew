import com.itsaky.androidide.build.config.BuildConfig

plugins {
  id("com.android.library")
  id("kotlin-android")
}

android { namespace = "${BuildConfig.packageName}.lsp.toml" }

dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
  implementation(projects.core.lspApi)
  implementation(projects.core.lspModels)
  implementation(projects.core.projects)
  implementation(libs.common.editor)
  implementation(projects.editor.lexers)
  implementation(libs.common.kotlin)
  implementation(libs.androidx.core.ktx)
}

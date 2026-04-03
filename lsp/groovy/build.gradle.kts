import com.itsaky.androidide.build.config.BuildConfig

plugins {
  id("com.android.library")
  id("kotlin-android")
}

android { namespace = "${BuildConfig.packageName}.lsp.groovy" }

dependencies {
  implementation(projects.core.lspApi)
  implementation(projects.core.lspModels)
  implementation(projects.core.projects)
  implementation(projects.editor.api)
  implementation(projects.editor.impl)
  implementation(projects.editor.lexers)
  implementation(libs.common.kotlin)
}

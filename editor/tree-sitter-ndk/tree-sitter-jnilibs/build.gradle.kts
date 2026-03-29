plugins { id("com.android.library") }

android {
  namespace = "com.itsaky.androidide.treesitter.xxx"

  defaultConfig { ndk { abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86")) } }
  buildFeatures { buildConfig = false }
  sourceSets { named("main") { jniLibs.srcDirs("src/main/jniLibs") } }
}

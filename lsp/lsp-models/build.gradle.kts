plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlinx-serialization") // 推荐用于某些元数据的本地持久化
}

android {
    namespace = "com.itsaky.androidide.lsp.models"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
}

dependencies {
    api(project(":lsp-rpc"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.core.ktx)
}
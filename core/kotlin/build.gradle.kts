import com.itsaky.androidide.build.config.BuildConfig
plugins {
    id("com.android.library")
    id("kotlin-android")
      
}

android {
    namespace = "android.zero.studio.kotlin"
    compileSdk = BuildConfig.compileSdk

    defaultConfig {
        minSdk = 21
    }

}

kotlin {
    jvmToolchain(17)
}

configurations.all {
    resolutionStrategy {
        force(libs.org.jetbrains.kotlin.stdlib)
        force(libs.hamcrest.all)
        force(libs.tests.junit)
        force(libs.common.lsp4j.jsonrpc)
        force(libs.common.org.eclipse.lsp4j)
        force(libs.org.jetbrains.kotlin.compiler)
        force(libs.org.jetbrains.kotlin.kotlin.scripting.jvm.host)
        force(libs.org.jetbrains.kotlin.ktscompiler)
        force(libs.org.jetbrains.kotlin.kts.jvm.host.unshaded)
        force(libs.org.jetbrains.kotlin.sam.with.receiver.compiler.plugin)
        force(libs.org.jetbrains.kotlin.reflect)
        force(libs.org.jetbrains.exposed.core)
        force(libs.org.jetbrains.exposed.dao)
        force(libs.org.jetbrains.exposed.jdbc)
        force(libs.com.google.guava.guava)
        
    }
          exclude(group = "org.hamcrest", module = "hamcrest-core")

}

dependencies {

    // api(libs.org.jetbrains.kotlin.compiler.embeddable){
        // exclude(group = "com.google.guava", module = "guava")
        // exclude(group = "org.jline", module = "jline")
        // exclude(group = "net.java.dev.jna", module = "jna-platform")
        // exclude(group = "net.java.dev.jna", module = "jna")
        // exclude(group = "org.jetbrains.kotlin", module = "kotlin-compiler")
        // exclude(group = "org.jetbrains.kotlin", module = "kotlin-scripting-jvm-host-unshaded")
        // exclude(group = "org.jetbrains.kotlin", module = "kotlin-compiler-embeddable")
        // exclude(group = "org.jetbrains.kotlin", module = "kotlin-scripting-compiler-embeddable")
        // exclude(group = "org.jetbrains.kotlin", module = "kotlin-scripting-compiler-impl-embeddable")
        // }
 
    implementation(libs.org.jetbrains.kotlin.stdlib)
    implementation(libs.org.jetbrains.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.core)
    
    implementation(libs.common.editor)

    //本地资源
    implementation(projects.termux.emulator)
    implementation(projects.termux.shared)
    implementation(projects.logging.logsender)
    api(projects.core.lspApi)
    api(projects.core.lspModels)
    implementation(projects.event.eventbus)
    implementation(projects.event.eventbusAndroid)
    implementation(projects.event.eventbusEvents)
    api(projects.core.projects)
    api(projects.utilities.lookup)
    api(projects.utilities.preferences)
    api(projects.core.common)
    implementation(projects.core.actions)
    
    // implementation(fileTree(mapOf("dir" to "lib", "include" to listOf("*.jar", "*.aar"))))
    implementation(files("libs/kotlin-compiler-2.2.0b.jar"))
    implementation(files("libs/kotlin-compiler-2.2.0.jar"))
    
    implementation(files("libs/j2k-services-222-1.8.21-377-IJ4167.29.jar"))
    implementation(files("libs/j2k-idea-233-1.9.30-dev-2838-IJ9999.jar"))
    implementation(files("libs/j2k-new-233-1.9.30-dev-2838-IJ9999.jar"))
    implementation(files("libs/j2k-old-233-1.9.30-dev-2838-IJ9999.jar"))
    implementation(files("libs/j2k-old-post-processing-233-1.9.30-dev-2838-IJ9999.jar"))
    implementation(files("libs/j2k-post-processing-233-1.9.30-dev-2838-IJ9999.jar"))
    implementation(libs.xml.javax.stream) //本地仓库资源：gradle/libs
    implementation(libs.com.github.fwcd.ktfmt) //本地仓库资源：gradle/libs
    implementation(libs.org.jetbrains.fernflower) //本地仓库资源：gradle/libs

        // --- 测试依赖 ---
    testImplementation(libs.tests.junit)
    testImplementation(libs.org.openjdk.jmh.core)
    
    
}

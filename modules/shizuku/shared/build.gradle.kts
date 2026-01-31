plugins {
	id("com.android.library")
}

android {
    namespace 'rikka.shizuku.shared'
    buildTypes {
        release {
            minifyEnabled false
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_11
        targetCompatibility JavaVersion.VERSION_11
    }
}

dependencies {
	implementation(projects.subprojects.shizukuAidl)
	implementation(libs.androidx.annotation)
}

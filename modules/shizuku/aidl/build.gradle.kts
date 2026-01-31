plugins {
	id("com.android.library")
}


android {
    namespace 'rikka.shizuku.aidl'
    buildFeatures {
        buildConfig false
        aidl true
    }
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

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.skyobservatory.renderer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.skyobservatory.renderer"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":api"))
    implementation(project(":engine"))
    implementation(libs.appcompat)
    implementation(libs.play.services.location)
}

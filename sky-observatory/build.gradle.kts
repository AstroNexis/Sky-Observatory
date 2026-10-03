plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.skyobservatory.renderer"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.skyobservatory.renderer"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.2"
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
    implementation(project(":engine"))
    implementation(project(":location"))
    implementation(libs.appcompat)
    implementation("io.github.tutorialsandroid:crashx:7.0.1")
}

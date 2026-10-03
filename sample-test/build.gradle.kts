plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.skyobservatory.sample"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.skyobservatory.sample"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    // The sample depends on the engine and location modules directly.
    // Native is pulled transitively through :engine.
    implementation(project(":api"))
    implementation(project(":engine"))
    implementation(project(":location"))

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.location)

    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso.core)
}

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.skyobservatory.sample"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.skyobservatory.sample"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
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
    // The sample only sees the api module directly. Engine and native are
    // pulled in transitively, which is the intended consumption pattern.
    implementation(project(":api"))
    implementation(project(":engine"))

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.location)

    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso.core)
}

plugins {
    alias(libs.plugins.android.library)
    id("jacoco")
}

android {
    namespace = "com.skyobservatory.engine"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    api(project(":api"))
    implementation(project(":native"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso.core)
}

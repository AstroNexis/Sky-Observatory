plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.skyobservatory.benchmark"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    implementation(project(":native"))

    testImplementation(libs.junit)
}

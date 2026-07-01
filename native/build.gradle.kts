plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.skyobservatory.native_bridge"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        externalNativeBuild {
            cmake {
                // Only build the core supernovas C library for the Android target.
                // Testing, examples, benchmarks, and optional plugins are excluded.
                arguments(
                    "-DBUILD_TESTING=OFF",
                    "-DBUILD_EXAMPLES=OFF",
                    "-DBUILD_BENCHMARK=OFF",
                    "-DBUILD_DOC=OFF",
                    "-DENABLE_CPP=OFF",
                    "-DENABLE_CALCEPH=OFF",
                    "-DENABLE_CSPICE=OFF",
                    "-DWITHOUT_CURL=ON"
                )
                cppFlags("-std=c++17")
            }
        }

        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.24.0+"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    api(project(":api"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso.core)
}

// Root build file. Module-specific configuration lives in each module's build.gradle.kts.

plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
}

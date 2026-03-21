// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {

    // Android
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false

    // Kotlin
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.parcelize) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Hilt
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false

    // Google
    alias(libs.plugins.google.services) apply false
}
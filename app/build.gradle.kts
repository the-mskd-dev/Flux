plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.parcelize)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.crashlytics)
}

android {
    namespace = "com.kaem.flux"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.kaem.flux"
        minSdk = 29
        targetSdk = 36
        versionCode = 2
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/*.kotlin_module"
        }
    }

}

dependencies {

    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.serialization.core)

    // Compose (Bundle + BOM)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)

    // Navigation 3
    implementation(libs.bundles.nav3)

    // Accompanist
    implementation(libs.accompanist.permissions)

    // Media Player
    implementation(libs.bundles.media3)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)
    ksp(libs.hilt.android.compiler)

    // Network & Serialization (Retrofit 3, OkHttp 5, Gson)
    implementation(libs.bundles.network)

    // Images
    implementation(libs.bundles.image)

    // DataStore
    implementation(libs.bundles.datastore)

    // Room
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    // Unit Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.mockk)
    implementation(libs.turbine)
    implementation(libs.kotlinx.coroutines.test)

    // Android Testing
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.androidx.core.testing)
    androidTestImplementation(libs.mockk.android)

    // UI Testing
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

}
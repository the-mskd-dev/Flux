import com.android.build.api.dsl.ApplicationExtension
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.parcelize)
    alias(libs.plugins.kotlin.compose)

    alias(libs.plugins.google.services) // PLAY_STORE
    alias(libs.plugins.firebase.crashlytics) // PLAY_STORE
}

// Local properties
val localPropertiesFile = rootProject.file("local.properties")
val localProperties = Properties()

if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

// Keystore properties
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()

if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

configure<ApplicationExtension> {
    namespace = "com.mskd.flux"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    ndkVersion = "29.0.13113456"

    defaultConfig {
        applicationId = "com.mskd.flux"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 37
        versionName = "1.8.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        val tmdbToken = localProperties.getProperty("tmdb_token") ?: ""
        buildConfigField("String", "TMDB_TOKEN", "\"$tmdbToken\"")
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("config") {
                keyAlias = keystoreProperties["keyAlias"]?.toString() ?: ""
                keyPassword = keystoreProperties["keyPassword"]?.toString() ?: ""
                storeFile =
                    keystoreProperties["storeFile"]?.toString()?.let { rootProject.file(it) }
                storePassword = keystoreProperties["storePassword"]?.toString() ?: ""
            }
        }
    }

    flavorDimensions += "distribution"

    productFlavors {
        create("foss") {
            dimension = "distribution"
            isDefault = true
        }
        create("playstore") {
            dimension = "distribution"
            applicationIdSuffix = ".playstore"
        }
    }

    buildTypes {

        release {
            if (signingConfigs.findByName("config") != null) {
                signingConfig = signingConfigs.getByName("config")
            }

            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            manifestPlaceholders["appName"] = "Flux Debug"
        }

        create("beta") {
            applicationIdSuffix = ".beta"
            versionNameSuffix = "-beta"
            manifestPlaceholders["appName"] = "Flux Beta"

            if (signingConfigs.findByName("config") != null) {
                signingConfig = signingConfigs.getByName("config")
            }

            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    sourceSets {
        getByName("androidTest") {
            resources.directories.add("$rootDir/shared/schemas")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
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

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

}

kotlin { jvmToolchain(21) }

dependencies {

    // KMP
    implementation(project(":shared"))

    // Compose (Bundle + BOM)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.android.compose)

    // Unit Testing
    testImplementation(libs.bundles.android.unit.test)

    // Android Testing
    androidTestImplementation(libs.bundles.android.test)

    // Firebase
    "playstoreImplementation"(platform(libs.firebase.bom)) // PLAY_STORE
    "playstoreImplementation"(libs.bundles.android.firebase) // PLAY_STORE

    // UI Testing
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debug
    debugImplementation(libs.bundles.android.compose.debug)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.configureEach {
    val isFossTask = name.contains("Foss", ignoreCase = true)

    val tasksToDisable = listOf(
        "GoogleServices",
        "Crashlytics"
    )

    if (isFossTask && tasksToDisable.any { name.contains(it) }) {
        enabled = false
    }
}
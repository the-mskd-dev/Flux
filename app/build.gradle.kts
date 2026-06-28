import com.android.build.api.dsl.ApplicationExtension
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.parcelize)
    alias(libs.plugins.kotlin.compose)
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
        versionCode = 27
        versionName = "1.5.2"

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

    // UI
    implementation(libs.bundles.android.ui)

    // Navigation 3
    implementation(libs.bundles.android.navigation)

    // Accompanist
    implementation(libs.bundles.android.accompanist)

    // ACRA
    implementation(libs.bundles.android.acra)

    // Unit Testing
    testImplementation(libs.bundles.android.unit.test)

    // Android Testing
    androidTestImplementation(libs.bundles.android.test)

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

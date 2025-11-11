plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.dopamindetox"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.dopamindetox"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true           // ✅ Compose 활성화 (중요)
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.6.11"   // ✅ 최신 Compose Compiler 버전
    }


    kotlinOptions {
        jvmTarget = "17"

        freeCompilerArgs += "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    val lifecycle = "2.8.6"
    val room = "2.8.3"
    val accompanist = "0.34.0"

    // ✅ Compose 핵심
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.google.android.material:material:1.12.0")

    // ✅ Lifecycle + ViewModel Compose
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle")

    // ✅ Room
    implementation("androidx.room:room-ktx:$room")
    kapt("androidx.room:room-compiler:$room")

    // ✅ Kotlin Core & DataStore
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ✅ Permissions / WorkManager
    implementation("com.google.accompanist:accompanist-permissions:$accompanist")
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")

    // ✅ Debug preview용
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.core:core-splashscreen:1.0.1")
}

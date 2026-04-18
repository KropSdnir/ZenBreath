plugins {
    alias(libs.plugins.android.application)
    // alias(libs.plugins.kotlin.android) // Attempting to remove this to avoid conflict
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.example.zenbreath"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.zenbreath"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Wear Compose
    implementation(libs.androidx.wear.compose.material3)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.wear.compose.ui.tooling)
    
    // Icons
    implementation("androidx.compose.material:material-icons-core")
    
    // Horologist
    implementation(libs.horologist.datalayer)
    implementation(libs.horologist.compose.layout)
    
    // Wearable Data Layer
    implementation(libs.play.services.wearable)
    implementation(libs.play.services.tasks)
    implementation(libs.kotlinx.coroutines.play.services)

    // Health Services
    implementation(libs.androidx.health.services.client)
    implementation(libs.guava)
    implementation(libs.kotlinx.coroutines.guava)
    
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")
    
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    
    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.2")
    
    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.10.0")
}

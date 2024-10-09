plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.alleysway"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.alleysway"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    
}



dependencies {
    implementation(libs.github.glide)
    implementation(libs.journeyapps.zxing.android.embedded)
    implementation(libs.core)
    implementation(libs.coil)
    implementation(libs.coil.gif)
    implementation(libs.coil.video) // AVIF support
    implementation(libs.kotlinx.coroutines.android)



    implementation(libs.google.play.services.auth.v2040)
    implementation(libs.google.api.client)
    implementation(libs.google.api.services.calendar)
    implementation(libs.play.services.auth)
    // Add Firebase Realtime Database dependency
    implementation(libs.firebase.database.ktx)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.androidx.ui.graphics.android)
    implementation(libs.androidx.material3.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.mpandroidchart.v310)



}
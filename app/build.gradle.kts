plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.techtitans.alleysway"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.techtitans.alleysway"
        minSdk = 26
        targetSdk = 34
        versionCode = 3
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

    buildFeatures {
        viewBinding = true
    }

    packagingOptions {
        resources{
            excludes += "META-INF/DEPENDENCIES"
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

    implementation(libs.play.services.auth)
    implementation(libs.firebase.database.ktx)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material) // use material here with the correct version reference
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation("androidx.cardview:cardview:1.0.0")

    // Google APIs Client Library for Java
    implementation("com.google.api-client:google-api-client-android:1.33.2")
    implementation("com.google.api-client:google-api-client-gson:1.33.2")
    implementation("com.google.http-client:google-http-client-jackson2:1.39.2")
    implementation("com.google.http-client:google-http-client-gson:1.39.2")
    implementation("com.google.api-client:google-api-client:1.23.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.23.0")
    implementation("com.google.apis:google-api-services-calendar:v3-rev305-1.23.0")

    //for the pfp on leaderboard page
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation(libs.androidx.ui.android)
    implementation(libs.androidx.material3.android)

    //for graphs
    implementation(libs.mpandroidchart.v310)
    implementation("com.google.android.material:material:1.8.0")
    implementation(libs.androidx.foundation.layout.android)


    testImplementation(libs.junit)
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.8.0")
    testImplementation ("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation("org.mockito:mockito-core:5.4.0")// Update to the latest version
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")// Latest version
    testImplementation ("org.robolectric:robolectric:4.9")

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}
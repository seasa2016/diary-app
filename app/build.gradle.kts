/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "2.1.20-2.0.1"
    id("org.jetbrains.kotlin.plugin.compose")

    id("com.google.gms.google-services")
}

android {
    compileSdk = 35

    defaultConfig {
        applicationId = "com.seasa.diary"
        minSdk = 34
        targetSdk = 35
        versionCode = 1
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "mozilla/public-suffix-list.txt"
        }
    }
    namespace = "com.seasa.diary"
}

configurations.all {
    resolutionStrategy {
        force( "com.google.guava:guava:31.0.1-android")
    }
}

dependencies {
    // Import the Compose BOM
    implementation(platform("androidx.compose:compose-bom:2025.06.00"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")
    implementation("androidx.navigation:navigation-compose:2.9.0")

    //Room
    implementation("androidx.room:room-runtime:2.7.1")
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("com.google.firebase:firebase-crashlytics-buildtools:3.0.4")
    implementation("com.android.volley:volley:1.2.1")
    ksp("androidx.room:room-compiler:2.7.1")
    implementation("androidx.room:room-ktx:2.7.1")

    // Google Drive API
    implementation("com.google.android.gms:play-services-drive:17.0.0")
    implementation("com.google.apis:google-api-services-drive:v3-rev136-1.25.0")
    implementation("androidx.credentials:credentials:<latest version>")
    implementation("androidx.credentials:credentials-play-services-auth:<latest version>")
    implementation("com.google.android.libraries.identity.googleid:googleid:<latest version>")
    implementation("com.google.http-client:google-http-client-gson:1.42.3")
    implementation("com.google.api-client:google-api-client-android:1.32.1")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0")

    // Layout
    implementation("androidx.compose.material3:material3:1.3.2")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("androidx.datastore:datastore-core:1.1.7")

    // Image
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Testing
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
}

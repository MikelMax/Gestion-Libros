plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.mikel.gestionlibrosv2"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mikel.gestionlibrosv2"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-runtime-ktx:2.8.4")
    implementation("androidx.navigation:navigation-compose:2.8.4")
    implementation("androidx.compose.runtime:runtime-livedata:1.7.5")
    implementation("androidx.compose.foundation:foundation-android:1.7.2")
    implementation("androidx.room:room-common:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("io.coil-kt:coil-compose:2.1.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("io.coil-kt:coil-compose:2.1.0")
    implementation("androidx.navigation:navigation-compose:2.8.4")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.compose.ui:ui:1.5.3")
    implementation("androidx.compose.material:material:1.5.3")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.ui:ui:1.7.5")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.runtime:runtime-livedata:1.7.5")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.material:material-icons-extended:1.5.3")
    implementation ("com.google.mlkit:text-recognition:16.0.0")
    implementation ("androidx.camera:camera-core:1.1.0")
    implementation ("androidx.camera:camera-camera2:1.1.0")
    implementation ("androidx.camera:camera-lifecycle:1.1.0")
    implementation ("androidx.camera:camera-view:1.1.0")

}
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.ctrlvnt.rytm"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ctrlvnt.rytm"
        minSdk = 24
        targetSdk = 34
        versionCode = 8
        versionName = "1.7"

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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("androidx.navigation:navigation-common:2.7.7")
    implementation("androidx.navigation:navigation-common-ktx:2.7.7")
    implementation("androidx.navigation:navigation-runtime-ktx:2.7.7")

    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")

    implementation ("com.github.bumptech.glide:glide:4.15.1")

    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation ("androidx.compose.material:material:1.7.5")
    implementation ("androidx.compose.ui:ui:1.7.5")
    implementation ("androidx.compose.ui:ui-tooling-preview:1.7.5")
}
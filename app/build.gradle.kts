import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.ctrlvnt.rytm"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ctrlvnt.rytm"
        minSdk = 24
        targetSdk = 35
        versionCode = 35
        versionName = "4.5"

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        viewBinding = true
    }

    //for F-Droid
    dependenciesInfo{
        includeInApk = false
        includeInBundle = false
    }
    configurations.all {
        exclude(group = "com.pierfrancescosoffritti.androidyoutubeplayer", module = "chromecast-sender")
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.1")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.1")
    implementation("androidx.navigation:navigation-common:2.9.1")
    implementation("androidx.navigation:navigation-common-ktx:2.9.1")
    implementation("androidx.navigation:navigation-runtime-ktx:2.9.1")

    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.2")

    implementation ("com.github.bumptech.glide:glide:4.16.0")

    implementation("androidx.room:room-runtime:2.7.2")
    ksp("androidx.room:room-compiler:2.7.2")

    implementation ("androidx.compose.material:material:1.8.3")
    implementation ("androidx.compose.ui:ui:1.8.3")
    implementation ("androidx.compose.ui:ui-tooling-preview:1.8.3")
    implementation ("com.google.android.material:material:1.12.0")
    implementation("com.github.AppIntro:AppIntro:6.3.1")
}

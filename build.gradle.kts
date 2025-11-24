// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.13.1" apply false
    id("org.jetbrains.kotlin.android") version "2.2.20" apply false
    id("com.google.devtools.ksp") version "2.1.21-2.0.2" apply false
}

buildscript {
    dependencies {
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.9.4")
    }
}

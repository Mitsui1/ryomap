// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    //alias(libs.plugins.android.application) apply false
    //alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin) apply false
    id("com.android.application") version "8.7.2" apply false
    }

    buildscript {
        dependencies {
            classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.5.0")
            classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
        }
    }


import java.io.FileInputStream
import java.util.Properties

plugins {
    //alias(libs.plugins.android.application)
    //alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    id("com.android.application")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

val properties = Properties()
val propertiesFile = rootProject.file("local.properties")
if (propertiesFile.exists()) {
    properties.load(FileInputStream(propertiesFile))
} else {
    throw GradleException("local.properties file not found!")
}

val mapsApiKey = properties["MAPS_API_KEY"] ?: "default_api_key"  // default 値を設定

android {
    namespace = "com.example.ryomap"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.ryomap"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "MAPS_API_KEY", "\"${"mapsApiKey"}\"")

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true

        buildConfig = true  // これを追加
    }
}

dependencies {

    /*implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)*/

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.squareup.picasso:picasso:2.8")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation(files("lib\\src\\main\\java\\com\\example\\lib\\mariadb-java-client-3.5.1.jar"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}


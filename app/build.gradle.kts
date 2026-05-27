plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.moduletask"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.moduletask"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


    implementation ("com.google.android.material:material:1.11.0")
    implementation ("com.github.yuriy-budiyev:code-scanner:2.3.0")
    implementation ("com.google.mlkit:barcode-scanning:17.3.0")
    implementation ("com.google.zxing:core:3.3.3")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.google.code.gson:gson:2.10.1")

}
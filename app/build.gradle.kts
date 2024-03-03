plugins {
    id  ("com.android.application")
    id  ("com.google.gms.google-services")

}

android {
    namespace = "com.example.dogblog"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.dogblog"
        minSdk = 26
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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
// Dependencies for UI and Support Libraries
        implementation ("androidx.appcompat:appcompat:1.6.1")  // AppCompat library for backward compatibility
        implementation ("com.google.android.material:material:1.11.0")  // Material Design library
        implementation ("androidx.constraintlayout:constraintlayout:2.1.4")  // ConstraintLayout library

// Firebase Dependencies
        implementation ("com.google.firebase:firebase-auth-ktx:22.3.1")  // Firebase Authentication library
        testImplementation ("junit:junit:4.13.2")  // JUnit testing framework
        androidTestImplementation ("androidx.test.ext:junit:1.1.5")  // AndroidX Test JUnit library
        androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")  // Espresso UI testing framework

// Firebase
        implementation ("com.google.firebase:firebase-bom:32.7.3")  // Firebase BoM for managing dependencies
        implementation ("com.google.firebase:firebase-analytics-ktx:21.5.1")  // Firebase Analytics library
        implementation ("com.google.firebase:firebase-database")  // Firebase Realtime Database library
        implementation ("com.google.firebase:firebase-core:21.1.1")  // Firebase Core library
        implementation ("com.google.firebase:firebase-auth")  // Firebase Authentication library
        implementation ("com.firebaseui:firebase-ui-auth:8.0.2")  // FirebaseUI Authentication library
        implementation ("com.firebaseui:firebase-ui-storage:7.2.0")  // FirebaseUI Storage library

// Image loading (Glide)
        implementation ("com.github.bumptech.glide:glide:4.16.0")  // Glide library for image loading
        annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")  // Glide annotation processor

// Circle image view
        implementation ("de.hdodenhof:circleimageview:3.1.0")  // CircleImageView library

// Lottie
        implementation ("com.airbnb.android:lottie:5.2.0")  // Lottie library for animations

    }


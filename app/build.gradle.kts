plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("kotlin-parcelize")
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = "com.example.pdfscanner"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.all.clear.pdf.scanner.mobile.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 11
        versionName = "11.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isCrunchPngs = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
//    applicationVariants.all {
//        val variant = this
//        variant.outputs
//            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
//            .filter {
//                val names = it.name.split("-")
//                it.name.lowercase().contains(names[0], true) && it.name.lowercase().contains(names[1], true)
//            }
//            .forEach { output ->
//                val outputFileName = "awesomeapp_${variant.flavorName}_${variant.buildType.name}_${variant.versionName}.apk"
//                output.outputFileName = outputFileName
//            }
//    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildTypes.all { isCrunchPngs = false }

//    alias: name
//    password: devconnect

    //... removed for brevity
    bundle {

        language {
            enableSplit = false
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //sdp ssp
    implementation("com.intuit.sdp:sdp-android:1.1.1")
    implementation("com.intuit.ssp:ssp-android:1.1.1")

    //View Pager
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("com.google.android.material:material:1.9.0")

    // Navigation Component
    val navVersion = "2.8.8"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")

    //ROOM DATABASE
    val roomVersion = "2.5.2" // Check for the latest version
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    //Coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    //SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    //Ml kit-Document-Scanner
    implementation("com.google.android.gms:play-services-mlkit-document-scanner:16.0.0-beta1")

    //Ml kit-Text_Recognition
    implementation("com.google.mlkit:text-recognition:16.0.1")

    //Animated Drawer
    implementation("com.github.mindinventory:minavdrawer:1.2.2")

//    Custom Toast
    implementation("com.github.emreesen27:Android-Custom-Toast-Message:1.0.5")

//    Custom progress bar
    implementation("com.github.ybq:Android-SpinKit:1.4.0")

//    Pdf Merge
    implementation("com.itextpdf:itextpdf:5.5.13.2")

//    Pdf Splitter
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")

//    Signature Pad
    implementation("com.github.gcacace:signature-pad:1.3.1")


    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    implementation("io.github.afreakyelf:Pdf-Viewer:2.2.2")

    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.3.1")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.3.1")

    // Kotlin Coroutines
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")

    implementation("com.airbnb.android:lottie:3.4.0")

    //    PDF Viewer
    implementation("com.github.mhiew:android-pdf-viewer:3.2.0-beta.3")

    implementation("com.github.jaiselrahman:FilePicker:1.3.2")
    implementation("com.vanniktech:android-image-cropper:4.6.0")

    implementation("com.google.android.play:review:2.0.2")
    implementation("com.google.android.play:review-ktx:2.0.2")

    implementation("com.google.android.gms:play-services-ads:24.4.0")

    implementation("com.android.billingclient:billing:6.0.1")
    implementation("com.android.billingclient:billing-ktx:6.0.1")

    implementation("com.facebook.shimmer:shimmer:0.5.0")

}
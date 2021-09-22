plugins {
    id(Plugins.library)
    id(Plugins.kotlinAndroid)
}

android {
    compileSdk = BuildConfig.compileSdkVersion
    buildToolsVersion = BuildConfig.buildToolsVersion

    defaultConfig {
        minSdk = BuildConfig.minSdkVersion
        targetSdk = BuildConfig.targetSdkVersion
        testInstrumentationRunner = BuildConfig.testInstrumentationRunner
    }

    buildFeatures {
        viewBinding = true
    }

    resourcePrefix = "emoji_"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = kotlinCompilerArgs
    }
}

dependencies {
    implementation(Libs.Kotlin.stdlib)
    implementation(Libs.Kotlin.reflect)
    implementation(Libs.Kotlin.coroutinesCore)
    implementation(Libs.Kotlin.coroutinesAndroid)
    implementation(Libs.Kotlin.coroutinesPlayServices)

    implementation(Libs.AndroidX.appcompat)
    implementation(Libs.AndroidX.coreKTX)
    implementation(Libs.AndroidX.fragment)
    implementation(Libs.AndroidX.constraintLayout)
    implementation(Libs.AndroidX.browser)
    implementation(Libs.AndroidX.multidex)
    implementation(Libs.AndroidX.legacySupportV4)
    implementation(Libs.AndroidX.Navigation.fragmentKTX)
    implementation(Libs.AndroidX.Navigation.uiKTX)
    implementation(Libs.AndroidX.Room.roomRuntime)
    implementation(Libs.AndroidX.Room.roomKTX)
    implementation(Libs.AndroidX.recyclerView)
    implementation(Libs.AndroidX.lifecycleProcess)

    implementation(Libs.Google.Android.material)

    implementation(Libs.Other.fastAdapter)
    Libs.Other.fastAdapterExtensions.forEach { implementation(it) }
    implementation(Libs.Other.timber)

    implementation(Libs.Other.tooltipWithArrow)
}
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
    multiDexEnabled = true
  }

  resourcePrefix = "emoji"

  buildFeatures {
    viewBinding = true
  }

  buildTypes {
    getByName("debug") {
      isMinifyEnabled = false
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  packagingOptions {
    resources.excludes.add("META-INF/main.kotlin_module")
  }

  kotlinOptions {
    jvmTarget = "1.8"
    freeCompilerArgs = kotlinCompilerArgs
  }
}

dependencies {
  api(project(":emoji"))
  implementation(Libs.AndroidX.appcompat)
}
plugins {
  id(Plugins.application)
  id(Plugins.kotlinAndroid)
}

android {
  compileSdk = BuildConfig.compileSdkVersion
  buildToolsVersion = BuildConfig.buildToolsVersion

  defaultConfig {
    applicationId = BuildConfig.applicationId
    minSdk = BuildConfig.minSdkVersion
    targetSdk = BuildConfig.targetSdkVersion
    versionCode = BuildConfig.versionCode
    versionName = BuildConfig.versionName
    testInstrumentationRunner = BuildConfig.testInstrumentationRunner
    multiDexEnabled = true
  }

  bundle {
    language {
      // Specifies that the app bundle should not support
      // configuration APKs for language resources. These
      // resources are instead packaged with each base and
      // dynamic feature APK.
      enableSplit = false
    }
  }

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
  api(project(":emoji-ios"))
  implementation(Libs.AndroidX.appcompat)
  implementation(Libs.AndroidX.constraintLayout)
}
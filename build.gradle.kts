buildscript {
  repositories {
    google()
    mavenCentral()
  }

  dependencies {
    classpath("com.android.tools.build:gradle:7.0.2")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlinVersion}")
  }
}

allprojects {
  repositories {
    google()
    mavenCentral()
  }
}
plugins {
    `kotlin-dsl`
}

allprojects {

    apply {
        plugin("org.gradle.kotlin.kotlin-dsl")
    }

    repositories {
        mavenCentral()
    }
}
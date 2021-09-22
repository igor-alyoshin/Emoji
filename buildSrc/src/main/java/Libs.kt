object Libs {
    object Kotlin {
        val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlinVersion}"
        val reflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlinVersion}"
        val coroutinesCore =
            "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutinesVersion}"
        val coroutinesAndroid =
            "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutinesVersion}"
        val coroutinesPlayServices =
            "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:${Versions.coroutinesVersion}"
    }

    object AndroidX {
        val fragment = "androidx.fragment:fragment:${Versions.materialVersion}"
        val appcompat = "androidx.appcompat:appcompat:${Versions.appcompatVersion}"
        val coreKTX = "androidx.core:core-ktx:${Versions.ktxVersion}"
        val recyclerView = "androidx.recyclerview:recyclerview:${Versions.recyclerViewVersion}"
        val lifecycleProcess = "androidx.lifecycle:lifecycle-process:${Versions.lifecycleVersion}"
        val constraintLayout =
            "androidx.constraintlayout:constraintlayout:${Versions.constraintLayoutVersion}"
        val browser = "androidx.browser:browser:${Versions.browserVersion}"
        val multidex = "androidx.multidex:multidex:${Versions.multidexVersion}"
        val legacySupportV4 = "androidx.legacy:legacy-support-v4:1.0.0"

        object Room {
            val roomCompiler = "androidx.room:room-compiler:${Versions.roomVersion}"
            val roomKTX = "androidx.room:room-ktx:${Versions.roomVersion}"
            val roomRuntime = "androidx.room:room-runtime:${Versions.roomVersion}"
        }

        object Navigation {
            val fragmentKTX = "androidx.navigation:navigation-fragment-ktx:${Versions.navVersion}"
            val uiKTX = "androidx.navigation:navigation-ui-ktx:${Versions.navVersion}"
        }
    }

    object Google {
        object Android {
            val material = "com.google.android.material:material:${Versions.materialVersion}"
        }
    }

    object Other {
        val fastAdapter = "com.mikepenz:fastadapter:${Versions.fastAdapterVersion}"
        val fastAdapterExtensions = listOf(
            "com.mikepenz:fastadapter-extensions-binding:${Versions.fastAdapterVersion}",
            "com.mikepenz:fastadapter-extensions-diff:${Versions.fastAdapterVersion}",
            "com.mikepenz:fastadapter-extensions-drag:${Versions.fastAdapterVersion}",
            "com.mikepenz:fastadapter-extensions-paged:${Versions.fastAdapterVersion}",
            "com.mikepenz:fastadapter-extensions-scroll:${Versions.fastAdapterVersion}",
            "com.mikepenz:fastadapter-extensions-swipe:${Versions.fastAdapterVersion}",
            "com.mikepenz:fastadapter-extensions-ui:${Versions.fastAdapterVersion}",
            "com.mikepenz:fastadapter-extensions-utils:${Versions.fastAdapterVersion}"
        )
        val tooltipWithArrow = "com.github.skydoves:balloon:${Versions.balloonVersion}"

        //Logs
        val timber = "com.jakewharton.timber:timber:${Versions.timberVersion}"
    }
}
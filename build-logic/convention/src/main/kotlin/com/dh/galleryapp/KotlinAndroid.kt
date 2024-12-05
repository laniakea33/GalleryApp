package com.dh.galleryapp

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

//  App, Android Library 모듈에서 사용됨
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.apply {
        compileSdk =
            libs.findVersion("projectCompileSdkVersion").get().toString().toInt()

        defaultConfig {
            minSdk =
                libs.findVersion("projectMinSdkVersion").get().toString().toInt()
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }

    dependencies {
        add("androidTestImplementation", libs.findLibrary("androidx.junit").get())
        add("androidTestImplementation", libs.findLibrary("androidx.espresso.core").get())
        add("androidTestImplementation", libs.findLibrary("androidx.ui.test.junit4").get())
    }

    configureKotlin()
}

//  Java/Kotlin, 즉 jvm 모듈에서 사용됨
internal fun Project.configureKotlinJvm() {
    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    configureKotlin()
}

//  모든 타입의 모듈에 공통 적용됨.
private fun Project.configureKotlin() {
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_17.toString()
        }
    }

    dependencies {
        add("testImplementation", libs.findLibrary("junit").get())
    }
}

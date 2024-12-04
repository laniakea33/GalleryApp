plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

//dependencies {
//    compileOnly(libs.android.gradlePlugin)
//    compileOnly(libs.android.tools.common)
//    compileOnly(libs.compose.gradlePlugin)
//    compileOnly(libs.kotlin.gradlePlugin)
//    compileOnly(libs.ksp.gradlePlugin)
//}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "galleryapp.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
    }
}
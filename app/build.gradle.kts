plugins {
    alias(libs.plugins.galleryapp.android.application)
    alias(libs.plugins.galleryapp.hilt)
    alias(libs.plugins.galleryapp.android.application.compose)
}

android {
    namespace = "com.dh.galleryapp"

    defaultConfig {
        applicationId = "com.dh.galleryapp"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

dependencies {
    //  Hilt Compose
    implementation(libs.androidx.hilt.navigation.compose)

    //  AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //  Hilt
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.hilt.android)

    //  프로젝트 내 모듈 참조
    implementation(projects.core.bitmap)
    implementation(projects.core.bitmapcache)
    implementation(projects.core.cache)
    implementation(projects.core.data)
    implementation(projects.core.common)
    implementation(projects.core.key)
    implementation(projects.core.ui)
    implementation(projects.core.model)
    implementation(projects.feature.list)
    implementation(projects.feature.detail)
}

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
    //  Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.compose.ui.test.manifest)

    //  Acvitity Compose
    implementation(libs.androidx.activity.compose)
    //  Hilt Compose
    implementation(libs.androidx.hilt.navigation.compose)
    // ViewModel Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    //  Paging Compose
    implementation(libs.androidx.paging.compose)

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

    //  Junit
    testImplementation(libs.junit)

    //  프로젝트 내 모듈 참조
    implementation(projects.core.bitmap)
    implementation(projects.core.bitmapcache)
    implementation(projects.core.cache)
    implementation(projects.core.data)
    implementation(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.key)
    implementation(projects.core.ui)
}

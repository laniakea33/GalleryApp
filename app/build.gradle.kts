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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.hilt.android)
    implementation(libs.core.ktx)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.moshi)
    implementation(libs.converter.moshi)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

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

plugins {
    alias(libs.plugins.galleryapp.android.library)
    alias(libs.plugins.galleryapp.android.library.compose)
    alias(libs.plugins.galleryapp.hilt)
    alias(libs.plugins.galleryapp.android.feature)
}

android {
    namespace = "com.dh.galleryapp.feature.detail"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.domain)
    implementation(projects.core.bitmap)
    implementation(projects.core.key)
    implementation(projects.feature.model)
}

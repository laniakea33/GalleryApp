plugins {
    alias(libs.plugins.galleryapp.android.library)
    alias(libs.plugins.galleryapp.hilt)
}

android {
    namespace = "com.dh.galleryapp.core.network"
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.moshi)
    implementation(libs.converter.moshi)

    implementation(projects.core.model)
}
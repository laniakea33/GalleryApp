plugins {
    alias(libs.plugins.galleryapp.android.library)
    alias(libs.plugins.galleryapp.android.library.compose)
    alias(libs.plugins.galleryapp.hilt)
    alias(libs.plugins.galleryapp.android.feature)
}

android {
    namespace = "com.dh.galleryapp.feature.list"
}

dependencies {
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.common)
    implementation(libs.androidx.paging.compose)

    implementation(projects.core.model)
    implementation(projects.core.domain)
    implementation(projects.core.bitmap)
    implementation(projects.core.bitmapcache)
    implementation(projects.core.key)
    implementation(projects.feature.model)
}

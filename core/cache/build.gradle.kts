plugins {
    alias(libs.plugins.galleryapp.android.library)
    alias(libs.plugins.galleryapp.hilt)
}

android {
    namespace = "com.dh.galleryapp.core.cache"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.storage)
}
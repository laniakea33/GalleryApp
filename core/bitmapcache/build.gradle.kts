plugins {
    alias(libs.plugins.galleryapp.android.library)
}

android {
    namespace = "com.dh.galleryapp.core.bitmapcache"
}

dependencies {
    implementation(projects.galleryApp.core.cache)
}
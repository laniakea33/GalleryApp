plugins {
    alias(libs.plugins.galleryapp.android.library)
}

android {
    namespace = "com.dh.galleryapp.core.bitmap"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}
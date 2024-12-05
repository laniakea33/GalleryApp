plugins {
    alias(libs.plugins.galleryapp.android.library)
    alias(libs.plugins.galleryapp.hilt)
}

android {
    namespace = "com.dh.galleryapp.core.data"
}

dependencies {
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.common)

    implementation(projects.core.model)
    implementation(projects.core.database)
    implementation(projects.core.network)
    implementation(projects.core.storage)
}

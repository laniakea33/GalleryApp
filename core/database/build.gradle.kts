plugins {
    alias(libs.plugins.galleryapp.android.library)
    alias(libs.plugins.galleryapp.hilt)
}

android {
    namespace = "com.dh.galleryapp.core.database"
}

dependencies {
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.common)
    implementation(libs.androidx.room.paging)
    implementation(libs.androidx.room.ktx)

    implementation(projects.core.model)
}
plugins {
    alias(libs.plugins.galleryapp.jvm.library)
    alias(libs.plugins.galleryapp.hilt)
}

dependencies {
    implementation(projects.core.model)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.paging.common)
}
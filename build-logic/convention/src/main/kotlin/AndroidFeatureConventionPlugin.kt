import com.dh.galleryapp.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            dependencies {
                //  Acvitity Compose
                add("implementation", libs.findLibrary("androidx.activity.compose").get())
                //  Hilt Compose
                add("implementation", libs.findLibrary("androidx.hilt.navigation.compose").get())
                // ViewModel Compose
                add(
                    "implementation",
                    libs.findLibrary("androidx.lifecycle.viewmodel.compose").get()
                )

                //  AndroidX
                add("implementation", libs.findLibrary("androidx.core.ktx").get())
                add("implementation", libs.findLibrary("androidx.lifecycle.runtime.ktx").get())
                add("implementation", libs.findLibrary("androidx.lifecycle.viewmodel.ktx").get())
                add("androidTestImplementation", libs.findLibrary("androidx.test.core.ktx").get())
                add("androidTestImplementation", libs.findLibrary("androidx.junit").get())
                add("androidTestImplementation", libs.findLibrary("androidx.espresso.core").get())

                add("implementation", project(":core:ui"))
            }
        }
    }
}

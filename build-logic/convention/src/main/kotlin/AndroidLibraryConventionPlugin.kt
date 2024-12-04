import com.android.build.gradle.LibraryExtension
import com.dh.galleryapp.configureKotlinAndroid
import com.dh.galleryapp.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig {
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                    targetSdk =
                        libs.findVersion("projectTargetSdkVersion").get().toString().toInt()
                }
            }
        }
    }
}

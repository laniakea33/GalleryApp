pluginManagement {
    includeBuild("build-logic")

    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "GalleryApp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app")
include(":core:bitmap")
include(":core:cache")
include(":core:data")
include(":core:domain")
include(":core:model")
include(":core:database")
include(":core:network")
include(":core:storage")
include(":core:serialization")
include(":core:key")
include(":core:ui")
include(":feature:list")
include(":feature:detail")
include(":feature:model")

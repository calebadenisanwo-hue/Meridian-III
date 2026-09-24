gradle.startParameter.projectProperties["android.useAndroidX"] = "true"
gradle.startParameter.projectProperties["android.nonTransitiveRClass"] = "true"

pluginManagement {
    repositories {
        google()
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

rootProject.name = "Meridian Personal Systems"
include(":app")

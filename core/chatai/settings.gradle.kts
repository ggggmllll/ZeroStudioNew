pluginManagement {
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
        maven("https://repo.itextsupport.com/android")
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "io.objectbox") {
                useModule("io.objectbox:objectbox-gradle-plugin:${requested.version}")
            }
        }
    }
}

rootProject.name = "arcade"

include("arcade-commands")
include("arcade-datagen")
include("arcade-dimensions")
include("arcade-events")
include("arcade-extensions")
include("arcade-items")
include("arcade-minigames")
include("arcade-resource-pack")
include("arcade-resource-pack-host")
include("arcade-scheduler")
include("arcade-utils")
include("arcade-visuals")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
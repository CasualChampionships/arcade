rootProject.name = "arcade"

include(
    ":arcade-boundaries",
    ":arcade-commands",
    ":arcade-datagen",
    ":arcade-dimensions",
    ":arcade-event-registry",
    ":arcade-events-client",
    ":arcade-events-server",
    ":arcade-extensions",
    ":arcade-items",
    ":arcade-minigames",
    ":arcade-nametags",
    ":arcade-npcs",
    ":arcade-resource-pack",
    ":arcade-resource-pack-host",
    ":arcade-scheduler",
    ":arcade-utils",
    ":arcade-visuals",
    ":arcade-world-border",
)

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
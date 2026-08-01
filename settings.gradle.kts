pluginManagement {
    includeBuild("build-logic")

    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        maven("https://maven4.bai.lol")
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "arcade"

include(
    ":arcade-boundaries",
    ":arcade-commands",
    ":arcade-datagen",
    ":arcade-debug",
    ":arcade-dimensions",
    ":arcade-event-registry",
    ":arcade-events-client",
    ":arcade-events-server",
    ":arcade-extensions",
    ":arcade-gametest",
    ":arcade-guis",
    ":arcade-interceptor",
    ":arcade-items",
    ":arcade-minigames",
    ":arcade-nametags",
    ":arcade-npcs",
    ":arcade-observers",
    ":arcade-replay",
    ":arcade-resource-pack",
    ":arcade-resource-pack-host",
    ":arcade-scheduler",
    ":arcade-utils",
    ":arcade-virtual-entities",
    ":arcade-visuals",
)

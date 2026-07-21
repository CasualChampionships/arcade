plugins {
    id("arcade.common-conventions")
}

dependencies {
    api(projects.arcadeUtils)
    api(projects.arcadeEventRegistry)
    api(projects.arcadeEventsServer)
    api(projects.arcadeExtensions)
    api(projects.arcadeResourcePackHost)

    api(libs.polymer.core)
    api(libs.polymer.resource.pack)
}

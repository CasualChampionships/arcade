plugins {
    id("arcade.common-conventions")
}

dependencies {
    api(projects.arcadeUtils)
    api(projects.arcadeEventRegistry)
    api(projects.arcadeEventsServer)

    implementation(projects.arcadeExtensions)
    implementation(projects.arcadeResourcePackHost)
}

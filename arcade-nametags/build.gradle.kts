plugins {
    id("arcade.common-conventions")
}

dependencies {
    api(projects.arcadeUtils)
    api(projects.arcadeExtensions)
    api(projects.arcadeVirtualEntities)
    api(projects.arcadeObservers)

    implementation(projects.arcadeEventRegistry)
    implementation(projects.arcadeEventsServer)
}

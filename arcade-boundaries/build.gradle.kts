plugins {
    id("arcade.common-conventions")
}

dependencies {
    api(projects.arcadeUtils)
    api(projects.arcadeExtensions)

    implementation(projects.arcadeEventRegistry)
    implementation(projects.arcadeEventsServer)
    implementation(projects.arcadeObservers)
    implementation(projects.arcadeVirtualEntities)
}

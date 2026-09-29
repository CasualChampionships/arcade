plugins {
    id("arcade.common-conventions")
}

dependencies {
    api(projects.arcadeUtils)
    api(projects.arcadeVirtualEntities)
    api(projects.arcadeObservers)

    implementation(projects.arcadeEventRegistry)
    implementation(projects.arcadeEventsServer)
    implementation(projects.arcadeExtensions)
    implementation(projects.arcadeScheduler)
    implementation(projects.arcadeResourcePack)
}

plugins {
    id("arcade.common-conventions")
}

dependencies {
    api(projects.arcadeUtils)
    api(projects.arcadeEventRegistry)
    api(projects.arcadeEventsServer)
    api(projects.arcadeExtensions)
    api(projects.arcadeScheduler)
    api(projects.arcadeResourcePack)
    api(projects.arcadeNametags)
    api(projects.arcadeVirtualEntities)
    api(projects.arcadeCommands)
}

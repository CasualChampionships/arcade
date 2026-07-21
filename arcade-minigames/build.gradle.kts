plugins {
    id("arcade.common-conventions")
}

dependencies {
    api(projects.arcadeUtils)
    api(projects.arcadeEventRegistry)
    api(projects.arcadeEventsServer)
    api(projects.arcadeExtensions)
    api(projects.arcadeGuis)
    api(projects.arcadeScheduler)
    api(projects.arcadeResourcePack)
    api(projects.arcadeVisuals)
    api(projects.arcadeCommands)
    api(projects.arcadeDimensions)
    api(projects.arcadeReplay)
}

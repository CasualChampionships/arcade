plugins {
    id("arcade.common-conventions")
}

dependencies {
    api(projects.arcadeUtils)
    api(projects.arcadeEventRegistry)
    api(projects.arcadeEventsServer)
}

loom {
    accessWidenerPath.set(file("src/main/resources/arcade-npcs.classtweaker"))
}

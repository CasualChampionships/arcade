plugins {
    id("arcade.common-conventions")
}

dependencies {
    api(projects.arcadeUtils)
    api(projects.arcadeEventRegistry)
    api(projects.arcadeEventsServer)
    api(projects.arcadeExtensions)
}

loom {
    accessWidenerPath.set(file("src/main/resources/arcade-dimensions.classtweaker"))
}

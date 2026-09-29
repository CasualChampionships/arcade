plugins {
    id("arcade.common-conventions")
}

dependencies {
    api(projects.arcadeUtils)

    implementation(projects.arcadeEventRegistry)
    implementation(projects.arcadeEventsServer)
    implementation(projects.arcadeExtensions)
}

loom {
    accessWidenerPath.set(file("src/main/resources/arcade-npcs.classtweaker"))
}

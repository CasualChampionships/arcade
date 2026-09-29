plugins {
    id("arcade.common-conventions")
}

dependencies {
    api(projects.arcadeUtils)
    api(projects.arcadeExtensions)

    implementation(projects.arcadeEventRegistry)
    implementation(projects.arcadeEventsServer)
}

loom {
    accessWidenerPath.set(file("src/main/resources/arcade-dimensions.classtweaker"))
}

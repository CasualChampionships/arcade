plugins {
    id("arcade.common-conventions")
}

dependencies {
    api(projects.arcadeUtils)
    api(projects.arcadeEventsServer)

    implementation(projects.arcadeEventRegistry)}
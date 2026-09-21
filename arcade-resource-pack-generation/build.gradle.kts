plugins {
    id("arcade.common-conventions")
}

dependencies {
    api(projects.arcadeResourcePack)
    api(projects.arcadeResourcePackHost)

    implementation(projects.arcadeUtils)
}

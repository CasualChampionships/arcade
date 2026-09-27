plugins {
    id("arcade.common-conventions")
}

dependencies {
    api(projects.arcadeNpcs)
    api(projects.arcadeUtils)

    implementation(projects.arcadeScheduler)

    compileOnly(projects.arcadeBoundaries)
    compileOnly(projects.arcadeMinigames)

    implementation(libs.reflections) {
        exclude(group = "org.slf4j")
    }
}

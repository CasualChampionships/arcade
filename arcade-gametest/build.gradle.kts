plugins {
    id("arcade.common-conventions")
}

dependencies {
    api(projects.arcadeNpcs)
    api(projects.arcadeScheduler)
    api(projects.arcadeUtils)

    compileOnly(projects.arcadeMinigames)

    implementation(libs.reflections) {
        exclude(group = "org.slf4j")
    }
}

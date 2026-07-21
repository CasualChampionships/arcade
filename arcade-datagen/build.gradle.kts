plugins {
    id("arcade.common-conventions")
}

dependencies {
    api(projects.arcadeResourcePack)
    api(projects.arcadeUtils)

    include(implementation("org.apache.commons:commons-text:1.11.0")!!)
}

tasks {
    register("runDatagenClient") {
        group = "fabric"
        runClient.get().args("--arcade-datagen")
        dependsOn(runClient)
    }
}

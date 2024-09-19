version = rootProject.version

dependencies {
    implementation(project(path = ":", configuration = "namedElements"))

    include(implementation("org.apache.commons:commons-text:1.11.0")!!)
}

tasks {
    register("runDatagenClient") {
        group = "fabric"
        runClient.get().args("--arcade-datagen")
        dependsOn(runClient)
    }
}
val moduleDependencies: (Project, List<String>) -> Unit by project

moduleDependencies(project, listOf("resource-pack", "utils"))

dependencies {
    modImplementation(libs.fabric.api)
    
    include(implementation("org.apache.commons:commons-text:1.11.0")!!)
}

tasks {
    register("runDatagenClient") {
        group = "fabric"
        runClient.get().args("--arcade-datagen")
        dependsOn(runClient)
    }
}
val moduleDependencies: (Project, List<String>) -> Unit by project

moduleDependencies(project, listOf("utils", "event-registry", "events-server"))

dependencies {
    // TODO:
    //  include(modApi(libs.debug.tools.api.get())!!)
}

loom {
    accessWidenerPath.set(file("src/main/resources/arcade-npcs.classtweaker"))
}

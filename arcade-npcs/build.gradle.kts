val moduleDependencies: (Project, List<String>) -> Unit by project

moduleDependencies(project, listOf("utils", "event-registry", "events-server"))

dependencies {
    modImplementation(libs.fabric.api)
    include(modApi(libs.debug.tools.api.get())!!)
}
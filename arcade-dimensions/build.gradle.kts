val moduleDependencies: (Project, List<String>) -> Unit by project

moduleDependencies(project, listOf("utils", "event-registry", "events-server", "extensions"))

dependencies {
    modImplementation(libs.fabric.api)
}
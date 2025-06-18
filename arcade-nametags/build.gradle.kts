val moduleDependencies: (Project, List<String>) -> Unit by project

moduleDependencies(project, listOf(
    "utils", "event-registry", "events-server", "extensions"
))

dependencies {
    modApi(libs.polymer.virtual.entity)
    modImplementation(libs.polymer.core)
}
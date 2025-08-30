val moduleDependencies: (Project, List<String>) -> Unit by project

moduleDependencies(project, listOf(
    "utils", "event-registry", "events-server", "extensions", "scheduler", "resource-pack", "visuals", "commands", "dimensions", "replay"
))

dependencies {
    modImplementation(libs.polymer.virtual.entity)
}
val moduleDependencies: (Project, List<String>) -> Unit by project

moduleDependencies(project, listOf(
    "utils", "event-registry", "events-server", "extensions", "scheduler", "resource-pack", "visuals", "commands", "dimensions"
))

dependencies {
    modCompileOnly(libs.server.replay)
    modImplementation(libs.polymer.virtual.entity)
}
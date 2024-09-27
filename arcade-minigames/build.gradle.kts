val moduleDependencies: (Project, List<String>) -> Unit by project

moduleDependencies(project, listOf(
    "utils", "events", "extensions", "scheduler", "resource-pack", "visuals", "commands", "dimensions"
))

dependencies {
    modApi(rootProject.libs.sgui)
    modApi(rootProject.libs.custom.nametags)

    modImplementation(rootProject.libs.server.translations)
    modImplementation(rootProject.libs.server.replay)
}
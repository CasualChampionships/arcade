val moduleDependencies: (Project, List<String>) -> Unit by project

moduleDependencies(project, listOf(
    "utils", "events", "extensions", "scheduler", "resource-pack", "visuals", "commands"
))

dependencies {
    modApi(rootProject.libs.sgui)
    modApi(rootProject.libs.custom.nametags)
    modApi(rootProject.libs.fantasy)

    modImplementation(rootProject.libs.server.replay)
}
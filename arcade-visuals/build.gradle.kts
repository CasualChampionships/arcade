version = rootProject.version

val moduleDependencies: (Project, List<String>) -> Unit by project

moduleDependencies(project, listOf(
    "utils", "events", "extensions", "scheduler", "resource-pack"
))

dependencies {
    modApi(rootProject.libs.sgui)
    modApi(rootProject.libs.custom.nametags)
}
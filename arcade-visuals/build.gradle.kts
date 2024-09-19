val moduleDependencies: (Project, List<String>) -> Unit by project

moduleDependencies(project, listOf(
    "utils", "events", "extensions", "scheduler", "resource-pack"
))

dependencies {
    include(modApi(rootProject.libs.sgui.get())!!)
    include(modApi(rootProject.libs.custom.nametags.get())!!)
}
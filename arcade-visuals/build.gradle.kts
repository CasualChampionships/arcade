val moduleDependencies: (Project, List<String>) -> Unit by project

moduleDependencies(project, listOf(
    "utils", "events", "extensions", "scheduler", "resource-pack"
))

dependencies {
    include(modApi(libs.sgui.get())!!)
    include(modApi(libs.custom.nametags.get())!!)
}
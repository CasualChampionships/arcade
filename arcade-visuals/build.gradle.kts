val moduleDependencies: (Project, List<String>) -> Unit by project

moduleDependencies(project, listOf(
    "utils", "event-registry", "events-server", "extensions", "scheduler", "resource-pack"
))

dependencies {
    include(modApi(libs.sgui.get())!!)
    include(modApi(libs.custom.nametags.get())!!)
    modImplementation(libs.polymer.virtual.entity)
}
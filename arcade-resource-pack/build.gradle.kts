val moduleDependencies: (Project, List<String>) -> Unit by project

moduleDependencies(project, listOf("utils", "events", "extensions", "resource-pack-host"))

dependencies {
    modApi(libs.polymer.core)
    modApi(libs.polymer.resource.pack)
}
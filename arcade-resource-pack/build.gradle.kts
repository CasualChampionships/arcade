val moduleDependencies: (Project, List<String>) -> Unit by project

moduleDependencies(project, listOf("utils", "event-registry", "events-server", "extensions", "resource-pack-host"))

dependencies {
    api(libs.polymer.core)
    api(libs.polymer.resource.pack)
}
version = rootProject.version

val moduleDependencies: (Project, List<String>) -> Unit by project

moduleDependencies(project, listOf("utils", "events", "extensions", "resource-pack-host"))

dependencies {
    modApi(rootProject.libs.polymer.core)
    modApi(rootProject.libs.polymer.resource.pack)
}
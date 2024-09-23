val moduleDependencies: (Project, List<String>) -> Unit by project

moduleDependencies(project, listOf("utils", "resource-pack"))

dependencies {
    modApi(rootProject.libs.polymer.core)
}
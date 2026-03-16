val moduleDependencies: (Project, List<String>) -> Unit by project

moduleDependencies(project, listOf("utils", "event-registry", "events-server"))

dependencies {
    compileOnly(libs.polymer.core)
}
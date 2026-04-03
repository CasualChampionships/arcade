val moduleDependencies: (Project, List<String>) -> Unit by project

moduleDependencies(project, listOf("utils", "event-registry", "events-server", "extensions"))

loom {
    accessWidenerPath.set(file("src/main/resources/arcade-dimensions.classtweaker"))
}

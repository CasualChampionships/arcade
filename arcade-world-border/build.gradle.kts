val moduleDependencies: (Project, List<String>) -> Unit by project

moduleDependencies(project, listOf("utils", "events", "extensions", "scheduler", "commands"))

loom {
    accessWidenerPath.set(file("src/main/resources/arcade-dimensions.accesswidener"))
}

dependencies {

}
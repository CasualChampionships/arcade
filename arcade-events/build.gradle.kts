val moduleDependencies: (Project, List<String>) -> Unit by project

moduleDependencies(project, listOf("utils"))
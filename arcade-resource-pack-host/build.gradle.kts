val moduleDependencies: (Project, List<String>) -> Unit by project

moduleDependencies(project, listOf("utils"))

dependencies {
    include(api(libs.inject.api.get())!!)
    include(api(libs.inject.http.get())!!)
    include(modApi(libs.inject.fabric.get())!!)
}
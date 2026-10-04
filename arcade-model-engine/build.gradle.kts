plugins {
    id("arcade.common-conventions")
}

dependencies {
    api(projects.arcadeObservers)
    api(projects.arcadeUtils)
    api(projects.arcadeVirtualEntities)

    include(implementation(libs.molang.compiler.get())!!)

    compileOnly(projects.arcadeResourcePackGeneration)
    testImplementation(projects.arcadeResourcePackGeneration)
}
plugins {
    id("arcade.common-conventions")
}

dependencies {
    implementation(projects.arcadeObservers)
    implementation(projects.arcadeUtils)
    implementation(projects.arcadeVirtualEntities)

//    api(projects.arcadeResourcePack)
    compileOnly(projects.arcadeResourcePackGeneration)

    include(implementation(libs.molang.compiler.get())!!)
}
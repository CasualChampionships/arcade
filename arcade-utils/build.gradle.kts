loom {
    accessWidenerPath.set(file("src/main/resources/arcade-utils.accesswidener"))
}

dependencies {
    modCompileOnly(libs.sgui)
    modCompileOnly(libs.polymer.core)
}

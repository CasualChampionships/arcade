loom {
    accessWidenerPath.set(file("src/main/resources/arcade-utils.classtweaker"))
}

dependencies {
    compileOnly(libs.sgui)
    compileOnly(libs.polymer.core)
}

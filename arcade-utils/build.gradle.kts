plugins {
    id("arcade.common-conventions")
}

dependencies {
    compileOnly(libs.sgui)
    compileOnly(libs.polymer.core)
}

loom {
    accessWidenerPath.set(file("src/main/resources/arcade-utils.classtweaker"))
}

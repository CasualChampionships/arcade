plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.plugins.kotlin.jvm.asDependency())
    implementation(libs.plugins.kotlin.serialization.asDependency())
    implementation(libs.plugins.fabric.loom.asDependency())
    implementation(libs.plugins.spotless.asDependency())

    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

fun Provider<PluginDependency>.asDependency() = map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version.requiredVersion}"
}
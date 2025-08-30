plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.explosion)
}

val shade: Configuration by configurations.creating

val moduleDependencies: (Project, List<String>) -> Unit by project

moduleDependencies(project, listOf("utils", "event-registry", "events-server", "resource-pack-host", "commands"))

dependencies {
    modCompileOnly(libs.carpet)
    modCompileOnly(libs.vmp)
    modCompileOnly(explosion.fabric(libs.c2me))
    modCompileOnly(libs.polymer.core)
    modCompileOnly(libs.voicechat)
    compileOnly(libs.voicechat.api)

    shade(api(libs.replay.studio.get())!!)
}

loom {
    accessWidenerPath.set(file("src/main/resources/arcade-replay.accesswidener"))
}

components {
    val component = getByName<AdhocComponentWithVariants>("java")
    component.withVariantsFromConfiguration(configurations.shadowRuntimeElements.get()) { skip() }
}

tasks {
    remapJar {
        inputFile.set(shadowJar.get().archiveFile)
    }

    shadowJar {
        destinationDirectory.set(File("./build/devlibs"))
        isZip64 = true

        from("LICENSE")

        // For compatability with viaversion
        relocate("assets/viaversion", "assets/replay-viaversion")

        relocate("com.github.steveice10.netty", "io.netty")
        exclude("com/github/steveice10/netty/**")

        exclude("it/unimi/dsi/**")
        exclude("org/apache/commons/**")
        exclude("org/xbill/DNS/**")
        exclude("com/google/**")

        configurations = listOf(shade)

        archiveClassifier = "shaded"
    }
}
plugins {
    id("arcade.common-conventions")
    alias(libs.plugins.shadow)
    alias(libs.plugins.explosion)
}

val shade: Configuration by configurations.creating

dependencies {
    api(projects.arcadeUtils)
    api(projects.arcadeEventRegistry)
    api(projects.arcadeEventsServer)
    api(projects.arcadeResourcePackHost)
    api(projects.arcadeCommands)

    compileOnly(projects.arcadeVirtualEntities)
    compileOnly(projects.arcadeVirtualVisuals)

    compileOnly(libs.carpet)
    compileOnly(libs.vmp)
    compileOnly(explosion.fabric(libs.c2me))
    compileOnly(libs.voicechat)
    compileOnly(libs.voicechat.api)

    shade(api(libs.replay.studio.get())!!)
}

loom {
    accessWidenerPath.set(file("src/main/resources/arcade-replay.classtweaker"))
}

tasks {
    shadowJar {
        archiveClassifier = ""

        isZip64 = true

        from("LICENSE")

        // For compatibility with viaversion
        relocate("assets/viaversion", "assets/replay-viaversion")

        relocate("com.github.steveice10.netty", "io.netty")
        exclude("com/github/steveice10/netty/**")

        exclude("it/unimi/dsi/**")
        exclude("org/apache/commons/**")
        exclude("org/xbill/DNS/**")
        exclude("com/google/**")

        configurations = listOf(shade)
    }

    jar {
        archiveClassifier = "slim"
    }
}

listOf("apiElements", "runtimeElements").forEach { configName ->
    configurations.named(configName) {
        outgoing {
            artifacts.clear()
            artifact(tasks.shadowJar)
        }
    }
}

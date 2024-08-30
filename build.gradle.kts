import org.apache.commons.io.output.ByteArrayOutputStream
import java.nio.charset.Charset

plugins {
    val jvmVersion = libs.versions.fabric.kotlin.get()
        .split("+kotlin.")[1]
        .split("+")[0]

    kotlin("jvm").version(jvmVersion)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.fabric.loom)
    `maven-publish`
    java
}

version = "1.0.0"
group = "net.casual"

allprojects {
    apply(plugin = "fabric-loom")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenLocal()
        maven("https://maven.parchmentmc.org/")
        maven("https://jitpack.io")
        maven("https://ueaj.dev/maven")
        maven("https://maven.nucleoid.xyz")
        maven("https://maven.maxhenkel.de/repository/public")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://repo.fruxz.dev/releases/")
        mavenCentral()
    }

    dependencies {
        val libs = rootProject.libs

        minecraft(libs.minecraft)
        @Suppress("UnstableApiUsage")
        mappings(loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-${libs.versions.parchment.get()}@zip")
        })

        modImplementation(libs.fabric.loader)
        modImplementation(libs.fabric.api)
        modImplementation(libs.fabric.kotlin)
    }

    kotlin {
        explicitApi()
    }

    java {
        withSourcesJar()
    }

    tasks {
        processResources {
            inputs.property("version", version)
            filesMatching("fabric.mod.json") {
                expand(mutableMapOf("version" to version))
            }
        }

        jar {
            from("LICENSE")
        }
    }
}

dependencies {
    includeModApi(libs.polymer.core)
    includeModApi(libs.polymer.blocks)
    includeModApi(libs.polymer.resource.pack)
    includeModApi(libs.polymer.virtual.entity)

    includeModApi(libs.fantasy)

    includeModApi(libs.permissions)
    includeModApi(libs.sgui)
    includeModApi(libs.server.translations)

    includeModApi(libs.custom.nametags)
    modApi(libs.server.replay)
}

loom {
    accessWidenerPath.set(file("src/main/resources/arcade.accesswidener"))
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(mutableMapOf("version" to project.version))
        }
    }

    jar {
        from("LICENSE")
    }
}

publishing {
    publications {
        create<MavenPublication>("arcade") {
            groupId = "com.github.CasualChampionships"
            artifactId = "arcade"
            version = getGitHash()
            from(components["java"])
        }
    }
}

fun getGitHash(): String {
    val out = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "HEAD")
        standardOutput = out
    }
    return out.toString(Charset.defaultCharset()).trim()
}

private fun DependencyHandler.includeModApi(dependencyNotation: Any) {
    include(dependencyNotation)
    modApi(dependencyNotation)
}
import org.apache.commons.io.output.ByteArrayOutputStream
import java.nio.charset.Charset

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.21"
    id("fabric-loom")
    `maven-publish`
    java
}

group = property("maven_group")!!
version = property("mod_version")!!

kotlin {
    explicitApi()
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://maven.parchmentmc.org/")
    }
    maven {
        url = uri("https://jitpack.io")
    }
    maven {
        url = uri("https://ueaj.dev/maven")
    }
    maven {
        url = uri("https://maven.nucleoid.xyz")
    }
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
}

configurations.all {
    resolutionStrategy {
        force("net.fabricmc:fabric-loader:${property("loader_version")}")
    }
}

@Suppress("UnstableApiUsage")
dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${property("parchment_version")}@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

    // modImplementation("xyz.nucleoid:server-translations-api:${property("server_translations_api_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")

    include(modApi("xyz.nucleoid:fantasy:${property("fantasy_version")}")!!)

    include(modApi("eu.pb4:polymer-core:${property("polymer_version")}")!!)
    include(modApi("eu.pb4:polymer-blocks:${property("polymer_version")}")!!)
    include(modApi("eu.pb4:polymer-resource-pack:${property("polymer_version")}")!!)
    include(modApi("eu.pb4:polymer-virtual-entity:${property("polymer_version")}")!!)

    include(modApi("com.github.senseiwells:CustomNameTags:${property("custom_nametags_version")}")!!)

    include(modApi("me.lucko:fabric-permissions-api:0.2-SNAPSHOT")!!)

    modImplementation("com.github.senseiwells:ServerReplay:${property("server_replay_version")}")
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

    publishing {
        publications {
            create<MavenPublication>("arcade") {
                groupId = "com.github.CasualChampionships"
                artifactId = "arcade"
                version = getGitHash()
                from(project.components.getByName("java"))
            }
        }

        repositories {

        }
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
}

java {
    withSourcesJar()
}

fun getGitHash(): String {
    val out = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "HEAD")
        standardOutput = out
    }
    return out.toString(Charset.defaultCharset()).trim()
}
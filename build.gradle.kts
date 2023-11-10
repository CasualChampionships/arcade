import org.apache.commons.io.output.ByteArrayOutputStream
import java.nio.charset.Charset

plugins {
    kotlin("jvm")
    id("fabric-loom")
    id("io.github.juuxel.loom-quiltflower")
    `maven-publish`
    java
}

group = property("maven_group")!!
version = property("mod_version")!!

kotlin {
    explicitApi()
}

repositories {
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

@Suppress("UnstableApiUsage")
dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${property("parchmentmc_version")}:${property("parchment_version")}@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

    // modImplementation("xyz.nucleoid:server-translations-api:${property("server_translations_api_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")

    include(modImplementation("eu.pb4:polymer-core:${property("polymer_version")}")!!)
    include(modImplementation("eu.pb4:polymer-blocks:${property("polymer_version")}")!!)
    include(modImplementation("eu.pb4:polymer-resource-pack:${property("polymer_version")}")!!)
    include(modImplementation("eu.pb4:polymer-virtual-entity:${property("polymer_version")}")!!)

    include(modImplementation("me.lucko:fabric-permissions-api:0.2-SNAPSHOT")!!)

    // include(modImplementation("eu.pb4:sgui:1.2.1+1.19.3")!!)
    include(implementation(annotationProcessor("com.github.llamalad7.mixinextras:mixinextras-fabric:${property("mixin_extras_version")}")!!)!!)
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
            create<MavenPublication>("mavenJava") {
                groupId = "com.github.CasualChampionships"
                artifactId = "arcade"
                version = getGitHash()
                artifact(remapJar) {
                    builtBy(remapJar)
                }
                artifact(kotlinSourcesJar) {
                    builtBy(remapSourcesJar)
                }
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
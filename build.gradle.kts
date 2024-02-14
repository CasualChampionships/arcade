import org.apache.commons.io.output.ByteArrayOutputStream
import java.nio.charset.Charset

plugins {
    kotlin("jvm")
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

sourceSets {
    val datagen by creating {
        compileClasspath += main.get().compileClasspath
        runtimeClasspath += main.get().runtimeClasspath
        compileClasspath += main.get().output
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

    include(modApi("me.lucko:fabric-permissions-api:0.3-SNAPSHOT")!!)

    "datagenImplementation"("org.apache.commons:commons-text:1.11.0")
}

loom {
    accessWidenerPath.set(file("src/main/resources/arcade.accesswidener"))

    mods {
        create("datagen") {
            sourceSet(sourceSets["datagen"])
        }
    }
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
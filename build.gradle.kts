import org.apache.commons.io.output.ByteArrayOutputStream
import java.nio.charset.Charset

plugins {
    kotlin("jvm")
    id("fabric-loom")
    id("io.github.juuxel.loom-quiltflower")
    id("com.github.johnrengelman.shadow")
    `maven-publish`
    java
}

group = property("maven_group")!!
version = property("mod_version")!!

val shade: Configuration by configurations.creating

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
    implementation(annotationProcessor("com.github.llamalad7.mixinextras:mixinextras-fabric:0.2.0-beta.6")!!)?.let { shade(it) }
}

loom {
    accessWidenerPath.set(file("src/main/resources/arcade.accesswidener"))
}

tasks.shadowJar {
    configurations = listOf(shade)

    // @see https://youtrack.jetbrains.com/issue/KT-25709
    exclude("**/*.kotlin_metadata")
    exclude("**/*.kotlin_builtins")

    relocate("com.llamalad7", "shadow.llamalad7")

    from("LICENSE")

    archiveClassifier.set("fat")

    // archiveFileName.set("${rootProject.name}-${archiveVersion.get()}.jar")
}

tasks.prepareRemapJar {
    dependsOn(tasks.shadowJar.get())
}

tasks.remapJar {
    remapperIsolation.set(true)
    inputFile.set(tasks.shadowJar.get().archiveFile.get())
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
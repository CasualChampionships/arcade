import org.apache.commons.io.output.ByteArrayOutputStream
import java.nio.charset.Charset

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.21"
    id("fabric-loom")
    `maven-publish`
    java
}

val modVersion: String by project
version = modVersion
group = "net.casual"

val minecraftVersion: String by project
val parchmentVersion: String by project
val loaderVersion: String by project
val fabricVersion: String by project
val fabricKotlinVersion: String by project

val fantasyVersion: String by project
val polymerVersion: String by project
val permissionsVersion: String by project
val customNametagsVersion: String by project
val serverReplayVersion: String by project

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
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://repo.fruxz.dev/releases/")
    }

    dependencies {
        minecraft("com.mojang:minecraft:$minecraftVersion")
        @Suppress("UnstableApiUsage")
        mappings(loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-$parchmentVersion@zip")
        })
        modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
        modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")

        modImplementation("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion")
    }

    kotlin {
        explicitApi()
    }

    java {
        withSourcesJar()
    }
}

configurations.all {
    resolutionStrategy {
        force("net.fabricmc:fabric-loader:$loaderVersion")
    }
}

dependencies {
    include(modApi("xyz.nucleoid:fantasy:$fantasyVersion")!!)

    include(modApi("eu.pb4:polymer-core:$polymerVersion")!!)
    include(modApi("eu.pb4:polymer-blocks:$polymerVersion")!!)
    include(modApi("eu.pb4:polymer-resource-pack:$polymerVersion")!!)
    include(modApi("eu.pb4:polymer-virtual-entity:$polymerVersion")!!)

    include(modApi("com.github.senseiwells:CustomNameTags:$customNametagsVersion")!!)

    include(modApi("me.lucko:fabric-permissions-api:$permissionsVersion")!!)

    modImplementation("com.github.senseiwells:ServerReplay:$serverReplayVersion")
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

    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
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
}

fun getGitHash(): String {
    val out = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "HEAD")
        standardOutput = out
    }
    return out.toString(Charset.defaultCharset()).trim()
}
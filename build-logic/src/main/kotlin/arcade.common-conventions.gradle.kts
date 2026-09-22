import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("net.fabricmc.fabric-loom")
    id("maven-publish")
    id("com.diffplug.spotless")
    java
}

val libs = the<LibrariesForLibs>()

val modVersion: String = providers.gradleProperty("mod_version").get()
val javaVersion: JavaLanguageVersion = JavaLanguageVersion.of(libs.versions.java.get())

group = "net.casualchampionships"
version = "${modVersion}+${libs.versions.minecraft.get()}"

repositories {
    mavenLocal()
    maven("https://maven.casualchampionships.net/snapshots")
    maven("https://masa.dy.fi/maven")
    maven("https://maven.parchmentmc.org/")
    maven("https://repo.viaversion.com")
    maven("https://jitpack.io")
    maven("https://api.modrinth.com/maven")
    maven("https://maven.nucleoid.xyz")
    maven("https://maven.maxhenkel.de/repository/public")
    maven("https://maven4.bai.lol")
    mavenCentral()
}

dependencies {
    minecraft(libs.minecraft)

    implementation(libs.fabric.loader)
    implementation(libs.fabric.kotlin)
    implementation(libs.fabric.api)

    testImplementation(libs.fabric.loader.junit)
    testImplementation(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()

    systemProperty("side", "SERVER")
}

java {
    toolchain {
        languageVersion.set(javaVersion)
    }
    withSourcesJar()
}

kotlin {
    explicitApi()
    jvmToolchain {
        languageVersion.set(javaVersion)
    }
}

tasks {
    processResources {
        inputs.property("version", modVersion)
        filesMatching("fabric.mod.json") {
            expand(mapOf(
                "version" to modVersion,
                "minecraft_dependency" to replaceVersion(libs.versions.minecraft.get(), "x"),
                "fabric_loader_dependency" to libs.versions.fabric.loader.get(),
                "fabric_api_dependency" to libs.versions.fabric.api.get(),
                "fabric_kotlin_dependency" to libs.versions.fabric.kotlin.get(),
                "polymer_dependency" to libs.versions.polymer.get(),
            ))
        }
    }

    jar {
        from(rootProject.file("LICENSE")) {
            rename { "arcade-LICENSE" }
        }
    }
}

loom {
    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    repositories {
        val mavenUrl = System.getenv("MAVEN_URL")
        if (mavenUrl != null) {
            maven {
                url = uri(mavenUrl)
                val mavenUsername = System.getenv("MAVEN_USERNAME")
                val mavenPassword = System.getenv("MAVEN_PASSWORD")
                if (mavenUsername != null && mavenPassword != null) {
                    credentials {
                        username = mavenUsername
                        password = mavenPassword
                    }
                }
            }
        }
    }
}

spotless {
    java {
        licenseHeaderFile(rootProject.file("HEADER")).yearSeparator("-")
        targetExclude("src/test/**", "src/gametest/**")
    }
    kotlin {
        licenseHeaderFile(rootProject.file("HEADER")).yearSeparator("-")
        targetExclude("src/test/**", "src/gametest/**")
    }
}

tasks.register("release") {
    group = "publishing"
    description = "Publishes this module to the remote Maven repository and to Maven local"
    dependsOn(tasks.named("publish"))
    dependsOn(tasks.named("publishToMavenLocal"))
}

if (project == rootProject) {
    val updateDocumentation = tasks.register<UpdateDocumentedDependencies>("updateDocumentedDependencies") {
        description = "Updates the arcade version in the README"
        documentationFile.set(rootProject.file("README.md"))
        module.set("arcade")
        arcadeVersion.set(provider { project.version.toString() })
        pluginVersion.set(libs.versions.joystick)
    }

    tasks.withType<AbstractPublishToMaven>().configureEach {
        dependsOn(updateDocumentation)
    }
}

private val minecraftVersionRegex = Regex("""^(\d+\.\d+)(\.\d+)?(?:-(pre|rc)-?(\d+))?$""")

fun replaceVersion(version: String, patch: String): String {
    val match = minecraftVersionRegex.matchEntire(version)
        ?: throw IllegalArgumentException("Unrecognised Minecraft version: $version")
    val (minor, patchVersion, type, number) = match.destructured
    if (type.isEmpty()) {
        return "$minor.$patch"
    }
    return "$minor$patchVersion-$type.$number"
}
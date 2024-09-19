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

val modVersion = "0.1.0-alpha.3"
version = "${modVersion}+${libs.versions.minecraft.get()}"
group = "net.casual-championships"

val moduleDependencies: Project.(List<String>) -> Unit by extra { { names ->
    dependencies {
        for (name in names) {
            api(project(path = ":arcade-$name", configuration = "namedElements"))
        }
    }
} }

allprojects {
    apply(plugin = "fabric-loom")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

    repositories {
        mavenLocal()
        maven("https://maven.supersanta.me/snapshots")
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
                val minecraftDependency = libs.versions.minecraft.get().replaceAfterLast('.', "x")
                expand(mutableMapOf(
                    "version" to version,
                    "minecraft_dependency" to minecraftDependency,
                ))
            }
        }

        jar {
            from("LICENSE")
        }
    }

    publishing {
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
}

subprojects {
    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
            }
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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            updateReadme("./README.md")
        }
    }
}

private fun DependencyHandler.includeModApi(dependencyNotation: Any) {
    include(dependencyNotation)
    modApi(dependencyNotation)
}

private fun MavenPublication.updateReadme(vararg readmes: String) {
    val location = "${groupId}:${artifactId}"
    val regex = Regex("""${Regex.escape(location)}:[\d\.\-a-zA-Z+]+""")
    val locationWithVersion = "${location}:${version}"
    for (path in readmes) {
        val readme = file(path)
        readme.writeText(readme.readText().replace(regex, locationWithVersion))
    }
}
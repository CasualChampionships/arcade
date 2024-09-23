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

val modVersion = "0.2.0-alpha.32"

allprojects {
    apply(plugin = "fabric-loom")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

    val libs = rootProject.libs

    group = "net.casual-championships"
    version = "${modVersion}+${libs.versions.minecraft.get()}"

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
    include(libs.polymer.core)
    include(libs.polymer.blocks)
    include(libs.polymer.resource.pack)
    include(libs.polymer.virtual.entity)

    include(libs.permissions)
    include(modImplementation(libs.server.translations.get())!!)

    for (subproject in project.subprojects) {
        if (subproject.path != ":arcade-datagen") {
            include(api(project(path = subproject.path, configuration = "namedElements"))!!)
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            updateReadme("./README.md")
        }
    }
}

val moduleDependencies: Project.(List<String>) -> Unit by extra { { names ->
    dependencies {
        for (name in names) {
            api(project(path = ":arcade-$name", configuration = "namedElements"))
        }
    }
} }

private fun MavenPublication.updateReadme(vararg readmes: String) {
    val location = "${groupId}:${artifactId}"
    val regex = Regex("""${Regex.escape(location)}:[\d\.\-a-zA-Z+]+""")
    val locationWithVersion = "${location}:${version}"
    for (path in readmes) {
        val readme = file(path)
        readme.writeText(readme.readText().replace(regex, locationWithVersion))
    }
}
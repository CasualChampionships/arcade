plugins {
    val jvmVersion = libs.versions.fabric.kotlin.get()
        .split("+kotlin.")[1]
        .split("+")[0]

    kotlin("jvm").version(jvmVersion)
    kotlin("plugin.serialization").version(jvmVersion)
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.spotless)
    `maven-publish`
    java
}

val modVersion = "0.8.1-beta.8"

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "fabric-loom")
    apply(plugin = "maven-publish")
    apply(plugin = "com.diffplug.spotless")

    val libs = rootProject.libs

    group = "net.casualchampionships"
    version = "${modVersion}+${libs.versions.minecraft.get()}"

    repositories {
        mavenLocal()
        maven("https://maven.supersanta.me/snapshots")
        maven("https://masa.dy.fi/maven")
        maven("https://maven.parchmentmc.org/")
        maven("https://repo.viaversion.com")
        maven("https://jitpack.io")
        maven("https://api.modrinth.com/maven")
        maven("https://maven.nucleoid.xyz")
        maven("https://maven.maxhenkel.de/repository/public")
        maven("https://maven.mcbrawls.net/releases/")
        maven("https://maven4.bai.lol")
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
        modImplementation(libs.fabric.kotlin)
        modImplementation(libs.fabric.api)
        modImplementation(libs.permissions)
    }

    kotlin {
        explicitApi()
    }

    java {
        withSourcesJar()
    }

    tasks {
        processResources {
            inputs.property("version", modVersion)
            filesMatching("fabric.mod.json") {
                val minecraftDependency = libs.versions.minecraft.get().replaceAfterLast('.', "x")
                expand(mutableMapOf(
                    "version" to modVersion,
                    "minecraft_dependency" to minecraftDependency,
                    "fabric_loader_dependency" to libs.versions.fabric.loader.get(),
                    "fabric_api_dependency" to libs.versions.fabric.api.get(),
                    "fabric_kotlin_dependency" to libs.versions.fabric.kotlin.get(),
                    "polymer_dependency" to libs.versions.polymer.get(),
                    "sgui_dependency" to libs.versions.sgui.get(),
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
            targetExclude("src/testmod/**")
        }
        kotlin {
            licenseHeaderFile(rootProject.file("HEADER")).yearSeparator("-")
            targetExclude("src/testmod/**")
        }
    }
}

subprojects {
    afterEvaluate {
//         updateDocumentedDependencies("../docs/${name}/getting-started.md")
    }
}

afterEvaluate {
    updateDocumentedDependencies("./README.md", false)
}

val testmod: SourceSet by sourceSets.creating {
    compileClasspath += sourceSets.main.get().compileClasspath
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
}

loom {
    runs {
        create("testmodServer") {
            server()
            source(testmod)
            vmArgs("-Dmixin.debug.export=true")
        }
    }
    createRemapConfigurations(testmod)
}

dependencies {
    include(libs.polymer.core)
    include(libs.polymer.blocks)
    include(libs.polymer.resource.pack)
    include(libs.polymer.virtual.entity)
    include(libs.permissions)

    include(modImplementation(libs.server.translations.get())!!)

//    "modTestmodRuntimeOnly"(libs.voicechat)

    val ignore = setOf(":arcade-datagen", ":arcade-events-client")
    for (subproject in project.subprojects) {
        if (subproject.path !in ignore) {
            api(project(path = subproject.path, configuration = "namedElements"))
            include(subproject)
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

private fun Project.updateDocumentedDependencies(path: String, transitive: Boolean = true) {
    val file = file(path)
    if (!file.exists()) {
        return
    }

    val builder = StringBuilder()
    builder.append("\ndependencies {\n")
    builder.append("""    include(modImplementation("${this.group}:${this.name}:${this.version}")!!)""")

    if (transitive) {
        val dependencies = configurations.api.get().dependencies.toMutableSet()
        dependencies.addAll(configurations.modApi.get().dependencies)
        dependencies.removeAll(configurations.include.get().dependencies)
        val shade = configurations.findByName("shade")
        if (shade != null) {
            dependencies.removeAll(shade.dependencies)
        }
        if (dependencies.isNotEmpty()) {
            dependencies.sortedBy { "${it.group}:${it.name}" }.joinTo(builder, "\n", "\n\n") {
                """    include(modImplementation("${it.group}:${it.name}:${it.version}")!!)"""
            }
        }
    }

    builder.append("\n}")
    builder.toString()
    val regex = Regex("""(\ndependencies \{[\s\S]+\})""")
    file.writeText(file.readText().replaceFirst(regex, builder.toString()))
}
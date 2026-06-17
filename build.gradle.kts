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

val modVersion = "0.10.0-beta.2"

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "net.fabricmc.fabric-loom")
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
        maven("https://maven4.bai.lol")
        mavenCentral()
    }

    dependencies {
        minecraft(libs.minecraft)

        implementation(libs.fabric.loader)
        implementation(libs.fabric.kotlin)
        implementation(libs.fabric.api)
    }

    java {
        withSourcesJar()
    }

    kotlin {
        explicitApi()
    }

    tasks {
        processResources {
            inputs.property("version", modVersion)
            filesMatching("fabric.mod.json") {
                expand(mutableMapOf(
                    "version" to modVersion,
                    "minecraft_dependency" to replaceVersion(libs.versions.minecraft.get(), "x"),
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
            sourceSet.set(testmod.name)
            jvmArguments.add("-Dmixin.debug.export=true")
            runDirectory.set(file("run/${libs.versions.minecraft.get()}"))
        }
    }
}

dependencies {
    include(libs.polymer.core)
    include(libs.polymer.blocks)
    include(libs.polymer.resource.pack)

    include(implementation(libs.server.translations.get())!!)

//    "testmodRuntimeOnly"(libs.voicechat)
    "testmodImplementation"(libs.reflections) {
        exclude(group = "org.slf4j")
    }

    val ignore = setOf(":arcade-datagen", ":arcade-events-client")
    for (subproject in project.subprojects) {
        if (subproject.path !in ignore) {
            api(project(subproject.path))
            include(subproject)
        }
    }
}

val moduleDependencies: Project.(List<String>) -> Unit by extra { { names ->
    dependencies {
        for (name in names) {
            api(project(":arcade-$name"))
        }
    }
} }

fun replaceVersion(version: String, patch: String): String {
    return version.replace(Regex("""^(\d+\.\d+)(\.\d+)?$"""), "$1.$patch")
}

private fun Project.updateDocumentedDependencies(path: String, transitive: Boolean = true) {
    val file = file(path)
    if (!file.exists()) {
        return
    }

    val builder = StringBuilder()
    builder.append("\ndependencies {\n")
    builder.append("""    include(implementation("${this.group}:${this.name}:${this.version}")!!)""")

    if (transitive) {
        val dependencies = configurations.api.get().dependencies.toMutableSet()
        dependencies.removeAll(configurations.include.get().dependencies)
        val shade = configurations.findByName("shade")
        if (shade != null) {
            dependencies.removeAll(shade.dependencies)
        }
        dependencies.removeAll { it.group?.startsWith("org.jetbrains.kotlin") == true }
        if (dependencies.isNotEmpty()) {
            dependencies.sortedBy { "${it.group}:${it.name}" }.joinTo(builder, "\n", "\n\n") {
                """    include(implementation("${it.group}:${it.name}:${it.version}")!!)"""
            }
        }
    }

    builder.append("\n}")
    builder.toString()
    val regex = Regex("""(\ndependencies \{[\s\S]+\})""")
    file.writeText(file.readText().replaceFirst(regex, builder.toString()))
}
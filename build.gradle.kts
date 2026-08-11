plugins {
    id("arcade.common-conventions")
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

fabricApi {
    @Suppress("UnstableApiUsage")
    configureTests {
        createSourceSet.set(true)
        modId.set("arcade-tests")

        enableGameTests.set(true)
        // TODO:
        enableClientGameTests.set(false)
        eula.set(true)
    }
}

loom {
    runs {
        named("gameTest") {
            systemProperties.put(
                "fabric-api.gametest.report-file",
                layout.buildDirectory.file("gametest-report.xml").get().asFile.absolutePath
            )
        }

        create("gameTestServer") {
            server()
            sourceSet.set("gametest")
            runDirectory.set(layout.projectDirectory.dir("run/gametest/${libs.versions.minecraft.get()}"))
        }
    }
}

dependencies {
    include(libs.polymer.core)
    include(libs.polymer.resource.pack)

    include(implementation(libs.server.translations.get())!!)

//    "testmodRuntimeOnly"(libs.voicechat)
    "testmodImplementation"(libs.reflections) {
        exclude(group = "org.slf4j")
    }

    "gametestImplementation"(projects.arcadeGametest)

    val ignore = setOf(projects.arcadeDatagen, projects.arcadeGametest).map { it.path }
    val hidden = setOf(projects.arcadeEventsClient).map { it.path }
    for (subproject in project.subprojects) {
        if (subproject.path in ignore) {
            continue
        }
        if (subproject.path in hidden) {
            implementation(subproject)
        } else {
            api(subproject)
        }
        include(subproject)
    }
}

data class ModVersion(val major: Int, val minor: Int, val patch: Int, val pre: String?) {
    override fun toString(): String {
        val core = "$major.$minor.$patch"
        return if (pre != null) "$core-$pre" else core
    }

    companion object {
        private val PATTERN = Regex("""^(\d+)\.(\d+)\.(\d+)(?:-(.+))?$""")

        fun parse(raw: String): ModVersion {
            val match = PATTERN.matchEntire(raw.trim())
                ?: error("Cannot parse mod_version '$raw' (expected MAJOR.MINOR.PATCH[-pre])")
            val (major, minor, patch) = match.destructured
            val pre = match.groupValues[4].ifEmpty { null }
            return ModVersion(major.toInt(), minor.toInt(), patch.toInt(), pre)
        }
    }
}

val modVersionProperties = rootProject.file("gradle.properties")

fun registerBumpTask(name: String, summary: String, bump: (ModVersion) -> ModVersion) {
    tasks.register(name) {
        group = "versioning"
        description = summary
        doLast {
            val text = modVersionProperties.readText()
            val current = ModVersion.parse(
                Regex("""(?m)^mod_version=(.*)$""").find(text)?.groupValues?.get(1)
                    ?: error("mod_version not found in gradle.properties")
            )
            val next = bump(current)
            modVersionProperties.writeText(
                text.replaceFirst(Regex("""(?m)^mod_version=.*$"""), "mod_version=$next")
            )
            logger.lifecycle("mod_version: $current -> $next")
        }
    }
}

registerBumpTask("bumpMajor", "Bumps to the next major version") {
    ModVersion(it.major + 1, 0, 0, null)
}

registerBumpTask("bumpMinor", "Bumps to the next minor version") {
    ModVersion(it.major, it.minor + 1, 0, null)
}

registerBumpTask("bumpPatch", "Bumps to the next patch version") {
    ModVersion(it.major, it.minor, it.patch + 1, null)
}

registerBumpTask("bumpBeta", "Bumps to the next beta version") { version ->
    val beta = Regex("""beta\.(\d+)""").matchEntire(version.pre ?: "")
    if (beta != null) {
        version.copy(pre = "beta.${beta.groupValues[1].toInt() + 1}")
    } else {
        version.copy(pre = "beta.1")
    }
}

tasks.register("newModule") {
    group = "scaffolding"
    description = "Scaffolds a new arcade module (usage: -Pmodule=arcade-<name> [-PmoduleName=\"Display Name\"])"
    doLast {

        val module = (findProperty("module") as String?)?.trim()
            ?: error("Missing required property, pass -Pmodule=arcade-<name>")
        require(module.matches(Regex("""arcade-[a-z0-9]+(-[a-z0-9]+)*"""))) { "Invalid module name: '$module'" }

        val moduleDir = rootProject.file(module)
        require(!moduleDir.exists()) { "Module directory already exists: $moduleDir" }

        val suffix = module.removePrefix("arcade-")
        val words = suffix.split("-")
        val displayName = words.joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
        val packageName = "net.casual.arcade.${suffix.replace("-", ".")}"
        val packagePath = packageName.replace(".", "/")
        val className = "Arcade" + words.joinToString("") { it.replaceFirstChar(Char::uppercase) }
        val coordinate = "net.casualchampionships:$module:${project.version}"

        fun write(relative: String, contents: String) {
            rootProject.file(relative).apply {
                parentFile.mkdirs()
                writeText(contents)
            }
        }

        write("$module/build.gradle.kts", """
            plugins {
                id("arcade.common-conventions")
            }
        """.trimIndent() + "\n")

        write("$module/src/main/kotlin/$packagePath/$className.kt", """
            package $packageName

            public object $className
        """.trimIndent() + "\n")

        val dollar = "$"
        write("$module/src/main/resources/fabric.mod.json", """
            {
              "schemaVersion": 1,
              "id": "$module",
              "version": "${dollar}{version}",
              "name": "Arcade $displayName",
              "description": "",
              "authors": [
                "Sensei"
              ],
              "contact": {

              },
              "license": "MIT",
              "icon": "assets/icon.png",
              "environment": "*",
              "depends": {
                "minecraft": "${dollar}{minecraft_dependency}",
                "fabricloader": ">=${dollar}{fabric_loader_dependency}",
                "fabric-language-kotlin": ">=${dollar}{fabric_kotlin_dependency}"
              },
              "custom": {
                "modmenu": {
                  "badges": ["library"],
                  "parent": {
                    "id": "arcade",
                    "name": "Arcade",
                    "icon": "assets/icon.png",
                    "badges": ["library"]
                  }
                }
              }
            }
        """.trimIndent() + "\n")

        write("docs/$module/getting-started.md", """
            # $displayName

            <!-- TODO: Describe what the $suffix module provides. -->

            ## Adding to Dependencies

            ```kts
            repositories {
                maven("https://maven.casualchampionships.net/snapshots")
            }

            dependencies {
                include(implementation("$coordinate")!!)
            }
            ```
        """.trimIndent() + "\n")

        val icon = rootProject.file("src/main/resources/assets/icon.png")
        if (icon.exists()) {
            icon.copyTo(rootProject.file("$module/src/main/resources/assets/icon.png"), overwrite = true)
        }

        val settings = rootProject.file("settings.gradle.kts")
        val text = settings.readText()
        if (!text.contains("\":$module\"")) {
            val lines = text.lines().toMutableList()
            val includeLine = Regex("""^\s*":arcade-[a-z0-9-]+",\s*$""")
            val indices = lines.indices.filter { includeLine.matches(lines[it]) }
            require(indices.isNotEmpty()) { "Could not locate module entries in settings.gradle.kts" }
            val insertAt = indices.firstOrNull {
                lines[it].trim().removeSurrounding("\"", "\",") > ":$module"
            } ?: (indices.last() + 1)
            lines.add(insertAt, "    \":$module\",")
            settings.writeText(lines.joinToString("\n"))
        }

        logger.lifecycle("Created module '$module' ($displayName):")
        logger.lifecycle("  $module/build.gradle.kts")
        logger.lifecycle("  $module/src/main/kotlin/$packagePath/$className.kt")
        logger.lifecycle("  $module/src/main/resources/fabric.mod.json")
        logger.lifecycle("  docs/$module/getting-started.md")
        logger.lifecycle("Registered ':$module' in settings.gradle.kts")
    }
}
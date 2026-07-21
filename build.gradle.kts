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

dependencies {
    include(libs.polymer.core)
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
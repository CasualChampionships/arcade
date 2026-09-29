import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Rewrites the build snippet inside the README so the documented versions
 * always match the current version.
 */
abstract class UpdateDocumentedDependencies: DefaultTask() {
    @get:Input
    abstract val module: Property<String>

    @get:Input
    abstract val arcadeVersion: Property<String>

    @get:Input
    abstract val pluginVersion: Property<String>

    @get:OutputFile
    abstract val documentationFile: RegularFileProperty

    init {
        group = "documentation"
        description = "Updates the documented build snippet with the current version"
    }

    @TaskAction
    fun update() {
        val snippet = """
        ```kts
        plugins {
            id("net.casualchampionships.joystick") version "${pluginVersion.get()}"
        }
        
        arcade {
            version = "${arcadeVersion.get()}"
            modules("${module.get()}")
        }
        ```
        """.trimIndent()

        val file = documentationFile.get().asFile
        val regex = Regex("""```kts\n(?:(?!```)[\s\S])*?(?:arcade \{|dependencies \{)[\s\S]*?\n```""")
        file.writeText(file.readText().replaceFirst(regex, Regex.escapeReplacement(snippet)))
    }
}

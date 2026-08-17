import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Rewrites the `dependencies { }` snippet inside a documentation file so the
 * documented coordinates always match the module's current version.
 */
abstract class UpdateDocumentedDependencies: DefaultTask() {
    @get:Input
    abstract val coordinate: Property<String>

    @get:Input
    abstract val includeTransitiveDependencies: Property<Boolean>

    @get:Input
    abstract val transitiveDependencies: ListProperty<String>

    @get:OutputFile
    abstract val documentationFile: RegularFileProperty

    init {
        group = "documentation"
        description = "Updates the documented dependency snippet with the current version"
    }

    @TaskAction
    fun update() {
        val builder = StringBuilder()
        builder.append("\ndependencies {\n")
        builder.append("""    include(implementation("${coordinate.get()}")!!)""")

        val transitive = transitiveDependencies.get()
        if (includeTransitiveDependencies.get() && transitive.isNotEmpty()) {
            transitive.joinTo(builder, "\n", "\n\n") {
                """    include(implementation("$it")!!)"""
            }
        }

        builder.append("\n}")

        val file = documentationFile.get().asFile
        val regex = Regex("""(\ndependencies \{[\s\S]+?\n})""")
        file.writeText(file.readText().replaceFirst(regex, builder.toString()))
    }
}

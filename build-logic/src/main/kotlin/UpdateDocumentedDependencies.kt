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
 *
 * All inputs are captured as serializable values at configuration time, so the
 * task is compatible with the configuration cache.
 */
abstract class UpdateDocumentedDependencies: DefaultTask() {
    /** The module's own `group:name:version` coordinate. */
    @get:Input
    abstract val coordinate: Property<String>

    /** Whether the documented snippet should also list transitive `api` dependencies. */
    @get:Input
    abstract val includeTransitiveDependencies: Property<Boolean>

    /** Sorted `group:name:version` coordinates of the transitive `api` dependencies. */
    @get:Input
    abstract val transitiveDependencies: ListProperty<String>

    /** The markdown file whose `dependencies { }` block is rewritten in place. */
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
        val regex = Regex("""(\ndependencies \{[\s\S]+})""")
        file.writeText(file.readText().replaceFirst(regex, builder.toString()))
    }
}
